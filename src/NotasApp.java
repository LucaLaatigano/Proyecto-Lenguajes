import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class NotasApp {

    // ==== MATERIAS DISPONIBLES ====
    static final String[] MATERIAS = {
            "Lenguajes II",
            "Estructura de Datos",
            "Sistemas I"
    };

    // ==== MODELO DE DATOS ====
    static class MateriaNotas {
        double[] parciales = new double[3];
        double[] tps = new double[2];
        double notaFinal = 0.0;
    }

    static class Student {
        String username;
        String nombre;
        Map<String, MateriaNotas> notasPorMateria = new HashMap<>();

        public Student(String username, String nombre) {
            this.username = username;
            this.nombre = nombre;
            // Inicializar las materias con estructuras vacías
            for (String m : MATERIAS) {
                notasPorMateria.put(m, new MateriaNotas());
            }
        }
    }

    static class User {
        String username;
        String password;
        boolean esProfesor;
        String alumnoUsername; // si es alumno, a cuál Student corresponde

        public User(String username, String password, boolean esProfesor, String alumnoUsername) {
            this.username = username;
            this.password = password;
            this.esProfesor = esProfesor;
            this.alumnoUsername = alumnoUsername;
        }
    }

    // "Base de datos" en memoria
    static Map<String, User> usuarios = new HashMap<>();
    static Map<String, Student> alumnos = new HashMap<>();

    public static void main(String[] args) {
        // Datos de ejemplo
        Student a1 = new Student("alu1", "Juan Pérez");
        Student a2 = new Student("alu2", "María Gómez");

        alumnos.put(a1.username, a1);
        alumnos.put(a2.username, a2);

        // Usuario profesor
        usuarios.put("prof", new User("prof", "1234", true, null));

        // Usuarios alumnos
        usuarios.put("alu1", new User("alu1", "1111", false, "alu1"));
        usuarios.put("alu2", new User("alu2", "2222", false, "alu2"));

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    // ==== FRAME LOGIN ====
    static class LoginFrame extends JFrame {
        private JTextField userField;
        private JPasswordField passField;

        public LoginFrame() {
            setTitle("Plataforma de Notas - Login");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(350, 200);
            setLocationRelativeTo(null);

            JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            panel.add(new JLabel("Usuario:"));
            userField = new JTextField();
            panel.add(userField);

            panel.add(new JLabel("Contraseña:"));
            passField = new JPasswordField();
            panel.add(passField);

            JButton loginBtn = new JButton("Iniciar sesión");
            loginBtn.addActionListener(e -> hacerLogin());

            panel.add(new JLabel()); // espacio
            panel.add(loginBtn);

            add(panel);
        }

        private void hacerLogin() {
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword());

            User u = usuarios.get(user);
            if (u == null || !u.password.equals(pass)) {
                JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Login correcto → elegir materia
            if (u.esProfesor) {
                new SeleccionMateriaFrame(true, null).setVisible(true);
            } else {
                Student s = alumnos.get(u.alumnoUsername);
                if (s != null) {
                    new SeleccionMateriaFrame(false, s).setVisible(true);
                }
            }
            dispose();
        }
    }

    // ==== FRAME SELECCIÓN DE MATERIA ====
    static class SeleccionMateriaFrame extends JFrame {
        private final boolean esProfesor;
        private final Student student; // solo si es alumno
        private JComboBox<String> comboMaterias;

        public SeleccionMateriaFrame(boolean esProfesor, Student student) {
            this.esProfesor = esProfesor;
            this.student = student;

            setTitle("Seleccionar materia");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(300, 150);
            setLocationRelativeTo(null);

            JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            panel.add(new JLabel("Elegí la materia:"));

            comboMaterias = new JComboBox<>(MATERIAS);
            panel.add(comboMaterias);

            JButton continuarBtn = new JButton("Continuar");
            continuarBtn.addActionListener(e -> continuar());
            panel.add(continuarBtn);

            add(panel);
        }

        private void continuar() {
            String materiaSeleccionada = (String) comboMaterias.getSelectedItem();
            if (materiaSeleccionada == null) return;

            if (esProfesor) {
                new ProfesorFrame(materiaSeleccionada).setVisible(true);
            } else {
                new AlumnoFrame(student, materiaSeleccionada).setVisible(true);
            }
            dispose();
        }
    }

    // ==== PANEL GRÁFICO ====
    static class GraficoNotasPanel extends JPanel {
        private MateriaNotas datos;

        public GraficoNotasPanel(MateriaNotas d) {
            this.datos = d;
            setPreferredSize(new Dimension(400, 250));
        }

        public void setDatos(MateriaNotas d) {
            this.datos = d;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (datos == null) return;

            double[] valores = new double[6];
            valores[0] = datos.parciales[0];
            valores[1] = datos.parciales[1];
            valores[2] = datos.parciales[2];
            valores[3] = datos.tps[0];
            valores[4] = datos.tps[1];
            valores[5] = datos.notaFinal;

            String[] etiquetas = { "P1", "P2", "P3", "TP1", "TP2", "Final" };

            int width = getWidth();
            int height = getHeight();
            int margen = 40;
            int anchoBarra = (width - 2 * margen) / valores.length;

            // Eje Y: hasta 10
            g.drawLine(margen, height - margen, width - margen, height - margen); // eje x
            g.drawLine(margen, margen, margen, height - margen); // eje y

            for (int i = 0; i < valores.length; i++) {
                double val = valores[i];
                if (val < 0) val = 0;
                if (val > 10) val = 10;

                int barHeight = (int) ((val / 10.0) * (height - 2 * margen));
                int x = margen + i * anchoBarra + 5;
                int y = height - margen - barHeight;

                g.fillRect(x, y, anchoBarra - 10, barHeight);
                g.drawString(etiquetas[i], x + (anchoBarra - 10) / 3, height - margen + 15);
            }

            g.drawString("Notas (0-10)", 5, 15);
        }
    }

    // ==== FRAME PROFESOR ====
    static class ProfesorFrame extends JFrame {
        private final String materia;
        private Student alumnoActual;
        private Student[] alumnosArray;
        private JTextField p1Field, p2Field, p3Field, tp1Field, tp2Field, finalField;
        private JLabel alumnoLabel;
        private GraficoNotasPanel graficoPanel;

        public ProfesorFrame(String materia) {
            this.materia = materia;

            setTitle("Panel Profesor - " + materia);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(800, 480);
            setLocationRelativeTo(null);

            // Crear arreglo de alumnos
            alumnosArray = alumnos.values().toArray(new Student[0]);
            if (alumnosArray.length > 0) {
                alumnoActual = alumnosArray[0];
            }

            // Panel superior: materia + botones
            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.add(new JLabel("Materia: " + materia));

            JButton listaBtn = new JButton("Lista alumnos");
            listaBtn.addActionListener(e -> mostrarListaAlumnos());
            topPanel.add(listaBtn);

            JButton verNotasBtn = new JButton("Cargar notas");
            verNotasBtn.addActionListener(e -> mostrarTablaNotas());
            topPanel.add(verNotasBtn);

            JButton materiasBtn = new JButton("Materias");
            materiasBtn.addActionListener(e -> {
                new SeleccionMateriaFrame(true, null).setVisible(true);
                dispose();
            });
            topPanel.add(materiasBtn);

            JButton logoutBtn = new JButton("Cerrar sesión");
            logoutBtn.addActionListener(e -> {
                new LoginFrame().setVisible(true);
                dispose();
            });
            topPanel.add(logoutBtn);

            // Panel central: alumno actual + campos de notas
            JPanel centerPanel = new JPanel(new GridLayout(7, 2, 5, 5));
            centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            centerPanel.add(new JLabel("Alumno actual:"));
            alumnoLabel = new JLabel(alumnoActual != null ? alumnoActual.username + " - " + alumnoActual.nombre : "Ninguno");
            centerPanel.add(alumnoLabel);

            centerPanel.add(new JLabel("Parcial 1:"));
            p1Field = new JTextField();
            centerPanel.add(p1Field);

            centerPanel.add(new JLabel("Parcial 2:"));
            p2Field = new JTextField();
            centerPanel.add(p2Field);

            centerPanel.add(new JLabel("Parcial 3:"));
            p3Field = new JTextField();
            centerPanel.add(p3Field);

            centerPanel.add(new JLabel("TP 1:"));
            tp1Field = new JTextField();
            centerPanel.add(tp1Field);

            centerPanel.add(new JLabel("TP 2:"));
            tp2Field = new JTextField();
            centerPanel.add(tp2Field);

            centerPanel.add(new JLabel("Nota final (calculada):"));
            finalField = new JTextField();
            finalField.setEditable(false);
            centerPanel.add(finalField);

            // Panel inferior: botón guardar + gráfico
            JPanel bottomPanel = new JPanel(new BorderLayout());
            JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton guardarBtn = new JButton("Calcular y guardar");
            guardarBtn.addActionListener(e -> guardarNotas());
            botonesPanel.add(guardarBtn);
            bottomPanel.add(botonesPanel, BorderLayout.NORTH);

            MateriaNotas datosIniciales = getDatosMateriaAlumnoActual();
            graficoPanel = new GraficoNotasPanel(datosIniciales);
            bottomPanel.add(graficoPanel, BorderLayout.CENTER);

            setLayout(new BorderLayout());
            add(topPanel, BorderLayout.NORTH);
            add(centerPanel, BorderLayout.CENTER);
            add(bottomPanel, BorderLayout.SOUTH);

            // Cargar datos del alumno actual
            cargarDatosAlumnoActual();
        }

        private MateriaNotas getDatosMateriaAlumnoActual() {
            if (alumnoActual == null) return null;
            MateriaNotas mn = alumnoActual.notasPorMateria.get(materia);
            if (mn == null) {
                mn = new MateriaNotas();
                alumnoActual.notasPorMateria.put(materia, mn);
            }
            return mn;
        }

        private void cargarDatosAlumnoActual() {
            MateriaNotas mn = getDatosMateriaAlumnoActual();
            if (mn == null) return;

            alumnoLabel.setText(alumnoActual.username + " - " + alumnoActual.nombre);
            p1Field.setText(String.valueOf(mn.parciales[0]));
            p2Field.setText(String.valueOf(mn.parciales[1]));
            p3Field.setText(String.valueOf(mn.parciales[2]));
            tp1Field.setText(String.valueOf(mn.tps[0]));
            tp2Field.setText(String.valueOf(mn.tps[1]));
            finalField.setText(String.format("%.2f", mn.notaFinal));

            graficoPanel.setDatos(mn);
        }

        private void mostrarListaAlumnos() {
            if (alumnosArray.length == 0) {
                JOptionPane.showMessageDialog(this, "No hay alumnos cargados.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] nombres = new String[alumnosArray.length];
            for (int i = 0; i < alumnosArray.length; i++) {
                nombres[i] = alumnosArray[i].username + " - " + alumnosArray[i].nombre;
            }

            JList<String> lista = new JList<>(nombres);
            lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scrollPane = new JScrollPane(lista);

            JDialog dialog = new JDialog(this, "Lista de alumnos", true);
            dialog.setSize(400, 300);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());
            dialog.add(scrollPane, BorderLayout.CENTER);

            JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton seleccionarBtn = new JButton("Seleccionar");
            seleccionarBtn.addActionListener(e -> {
                int idx = lista.getSelectedIndex();
                if (idx >= 0) {
                    alumnoActual = alumnosArray[idx];
                    cargarDatosAlumnoActual();
                    dialog.dispose();
                }
            });
            JButton cancelarBtn = new JButton("Cancelar");
            cancelarBtn.addActionListener(e -> dialog.dispose());
            botones.add(seleccionarBtn);
            botones.add(cancelarBtn);
            dialog.add(botones, BorderLayout.SOUTH);

            dialog.setVisible(true);
        }

        private void mostrarTablaNotas() {
            if (alumnosArray.length == 0) {
                JOptionPane.showMessageDialog(this, "No hay alumnos cargados.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] columnas = {"Usuario", "Nombre", "P1", "P2", "P3", "TP1", "TP2", "Final"};
            Object[][] data = new Object[alumnosArray.length][8];

            for (int i = 0; i < alumnosArray.length; i++) {
                Student s = alumnosArray[i];
                MateriaNotas mn = s.notasPorMateria.get(materia);
                if (mn == null) mn = new MateriaNotas();

                data[i][0] = s.username;
                data[i][1] = s.nombre;
                data[i][2] = mn.parciales[0];
                data[i][3] = mn.parciales[1];
                data[i][4] = mn.parciales[2];
                data[i][5] = mn.tps[0];
                data[i][6] = mn.tps[1];
                data[i][7] = mn.notaFinal;
            }

            JTable tabla = new JTable(data, columnas);
            JScrollPane scrollPane = new JScrollPane(tabla);

            JDialog dialog = new JDialog(this, "Notas de todos los alumnos - " + materia, true);
            dialog.setSize(700, 300);
            dialog.setLocationRelativeTo(this);
            dialog.add(scrollPane);
            dialog.setVisible(true);
        }

        private void guardarNotas() {
            if (alumnoActual == null) return;

            try {
                MateriaNotas mn = getDatosMateriaAlumnoActual();

                mn.parciales[0] = Double.parseDouble(p1Field.getText());
                mn.parciales[1] = Double.parseDouble(p2Field.getText());
                mn.parciales[2] = Double.parseDouble(p3Field.getText());
                mn.tps[0] = Double.parseDouble(tp1Field.getText());
                mn.tps[1] = Double.parseDouble(tp2Field.getText());

                mn.notaFinal = calcularNotaFinal(mn);

                finalField.setText(String.format("%.2f", mn.notaFinal));
                graficoPanel.repaint();

                JOptionPane.showMessageDialog(this,
                        "Notas guardadas correctamente para " + alumnoActual.nombre + " en " + materia,
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Ingresá solo números en las notas (usar punto para decimales).",
                        "Error de formato", JOptionPane.ERROR_MESSAGE);
            }
        }

        private double calcularNotaFinal(MateriaNotas mn) {
            double p1 = mn.parciales[0];
            double p2 = mn.parciales[1];
            double p3 = mn.parciales[2];
            double tp1 = mn.tps[0];
            double tp2 = mn.tps[1];

            return p1 * 0.20 + p2 * 0.25 + p3 * 0.25 + tp1 * 0.15 + tp2 * 0.15;
        }
    }

    // ==== FRAME ALUMNO ====
    static class AlumnoFrame extends JFrame {
        private final Student student;
        private final String materia;
        private JLabel p1Label, p2Label, p3Label, tp1Label, tp2Label, finalLabel;
        private GraficoNotasPanel graficoPanel;

        public AlumnoFrame(Student s, String materia) {
            this.student = s;
            this.materia = materia;

            setTitle("Panel Alumno - " + s.nombre + " (" + materia + ")");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setSize(550, 380);
            setLocationRelativeTo(null);

            JPanel infoPanel = new JPanel(new GridLayout(6, 2, 5, 5));
            infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            infoPanel.add(new JLabel("Parcial 1:"));
            p1Label = new JLabel();
            infoPanel.add(p1Label);

            infoPanel.add(new JLabel("Parcial 2:"));
            p2Label = new JLabel();
            infoPanel.add(p2Label);

            infoPanel.add(new JLabel("Parcial 3:"));
            p3Label = new JLabel();
            infoPanel.add(p3Label);

            infoPanel.add(new JLabel("TP 1:"));
            tp1Label = new JLabel();
            infoPanel.add(tp1Label);

            infoPanel.add(new JLabel("TP 2:"));
            tp2Label = new JLabel();
            infoPanel.add(tp2Label);

            infoPanel.add(new JLabel("Nota final:"));
            finalLabel = new JLabel();
            infoPanel.add(finalLabel);

            MateriaNotas mn = student.notasPorMateria.get(materia);
            if (mn == null) {
                mn = new MateriaNotas();
                student.notasPorMateria.put(materia, mn);
            }
            graficoPanel = new GraficoNotasPanel(mn);

            // Botones: Materias + Cerrar sesión
            JButton materiasBtn = new JButton("Materias");
            materiasBtn.addActionListener(e -> {
                new SeleccionMateriaFrame(false, student).setVisible(true);
                dispose();
            });

            JButton logoutBtn = new JButton("Cerrar sesión");
            logoutBtn.addActionListener(e -> {
                new LoginFrame().setVisible(true);
                dispose();
            });

            JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            botonesPanel.add(materiasBtn);
            botonesPanel.add(logoutBtn);

            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.add(graficoPanel, BorderLayout.CENTER);
            bottomPanel.add(botonesPanel, BorderLayout.SOUTH);

            setLayout(new BorderLayout());
            add(infoPanel, BorderLayout.NORTH);
            add(bottomPanel, BorderLayout.CENTER);

            actualizarLabels();
        }

        private void actualizarLabels() {
            MateriaNotas mn = student.notasPorMateria.get(materia);
            if (mn == null) return;

            p1Label.setText(String.valueOf(mn.parciales[0]));
            p2Label.setText(String.valueOf(mn.parciales[1]));
            p3Label.setText(String.valueOf(mn.parciales[2]));
            tp1Label.setText(String.valueOf(mn.tps[0]));
            tp2Label.setText(String.valueOf(mn.tps[1]));
            finalLabel.setText(String.format("%.2f", mn.notaFinal));
        }
    }
}