import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class NotasApp {

    static final String ARCHIVO_DATOS = "notas_data.bin";

    static final String[] MATERIAS = {
            "Lenguajes II",
            "Estructura de Datos",
            "Sistemas I"
    };

    static class MateriaNotas implements Serializable {
        private static final long serialVersionUID = 1L;
        double[] parciales = new double[3];
        double[] tps = new double[2];
        double notaFinal = 0.0;
    }

    static class Student implements Serializable {
        private static final long serialVersionUID = 1L;
        String username;
        String nombre;
        Map<String, MateriaNotas> notasPorMateria = new HashMap<>();

        public Student(String username, String nombre) {
            this.username = username;
            this.nombre = nombre;
            for (String m : MATERIAS) {
                notasPorMateria.put(m, new MateriaNotas());
            }
        }
    }

    static class User {
        String username;
        String password;
        boolean esProfesor;
        String alumnoUsername;

        public User(String username, String password, boolean esProfesor, String alumnoUsername) {
            this.username = username;
            this.password = password;
            this.esProfesor = esProfesor;
            this.alumnoUsername = alumnoUsername;
        }
    }

    static Map<String, User> usuarios = new HashMap<>();
    static Map<String, Student> alumnos = new HashMap<>();

    public static void main(String[] args) {
        cargarDatos();

        if (alumnos.isEmpty()) {
            Student a1 = new Student("alu1", "Juan Pérez");
            Student a2 = new Student("alu2", "María Gómez");

            alumnos.put(a1.username, a1);
            alumnos.put(a2.username, a2);

            guardarDatos();
        }

        usuarios.put("prof", new User("prof", "1234", true, null));
        usuarios.put("alu1", new User("alu1", "1111", false, "alu1"));
        usuarios.put("alu2", new User("alu2", "2222", false, "alu2"));

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    static void guardarDatos() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(ARCHIVO_DATOS))) {
            out.writeObject(alumnos);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al guardar datos: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    static void cargarDatos() {
        File archivo = new File(ARCHIVO_DATOS);
        if (!archivo.exists()) return;

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(archivo))) {
            alumnos = (Map<String, Student>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

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

            panel.add(new JLabel());
            panel.add(loginBtn);

            add(panel);

            getRootPane().setDefaultButton(loginBtn);
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

    static class SeleccionMateriaFrame extends JFrame {
        private final boolean esProfesor;
        private final Student student;
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

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double[] valores = {
                    datos.parciales[0], datos.parciales[1], datos.parciales[2],
                    datos.tps[0], datos.tps[1], datos.notaFinal
            };

            String[] etiquetas = { "P1", "P2", "P3", "TP1", "TP2", "Final" };

            int width = getWidth();
            int height = getHeight();
            int margen = 40;
            int anchoBarra = (width - 2 * margen) / valores.length;

            g2.setColor(Color.BLACK);
            g2.drawLine(margen, height - margen, width - margen, height - margen);
            g2.drawLine(margen, margen, margen, height - margen);

            for (int i = 0; i < valores.length; i++) {
                double val = valores[i];
                if (val < 0) val = 0;
                if (val > 10) val = 10;

                int barHeight = (int) ((val / 10.0) * (height - 2 * margen));
                int x = margen + i * anchoBarra + 10;
                int y = height - margen - barHeight;

                if (i == 5) {
                    if (val >= 7) g2.setColor(new Color(50, 200, 50));
                    else if (val >= 4) g2.setColor(new Color(255, 200, 0));
                    else g2.setColor(new Color(220, 50, 50));
                } else {
                    g2.setColor(new Color(100, 150, 240));
                }

                g2.fillRect(x, y, anchoBarra - 20, barHeight);

                g2.setColor(Color.BLACK);
                g2.drawRect(x, y, anchoBarra - 20, barHeight);

                g2.drawString(etiquetas[i], x + (anchoBarra - 20) / 3, height - margen + 15);
                g2.drawString(String.format("%.1f", val), x + (anchoBarra - 20) / 3, y - 5);
            }

            g2.drawString("Escala (0-10)", 5, 15);
        }
    }

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
            setSize(850, 520);
            setLocationRelativeTo(null);

            alumnosArray = alumnos.values().toArray(new Student[0]);
            if (alumnosArray.length > 0) {
                alumnoActual = alumnosArray[0];
            }

            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.add(new JLabel("Materia: " + materia));

            JButton listaBtn = new JButton("Lista alumnos");
            listaBtn.addActionListener(e -> mostrarListaAlumnos());
            topPanel.add(listaBtn);

            JButton verNotasBtn = new JButton("Ver tabla global");
            verNotasBtn.addActionListener(e -> mostrarTablaNotas());
            topPanel.add(verNotasBtn);

            JButton materiasBtn = new JButton("Cambiar Materia");
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

            JPanel centerPanel = new JPanel(new GridLayout(7, 2, 5, 5));
            centerPanel.setBorder(BorderFactory.createTitledBorder("Carga de Notas"));

            centerPanel.add(new JLabel("Alumno actual:"));
            alumnoLabel = new JLabel(alumnoActual != null ? alumnoActual.username + " - " + alumnoActual.nombre : "Ninguno");
            alumnoLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            centerPanel.add(alumnoLabel);

            centerPanel.add(new JLabel("Parcial 1 (20%):"));
            p1Field = new JTextField(); centerPanel.add(p1Field);

            centerPanel.add(new JLabel("Parcial 2 (25%):"));
            p2Field = new JTextField(); centerPanel.add(p2Field);

            centerPanel.add(new JLabel("Parcial 3 (25%):"));
            p3Field = new JTextField(); centerPanel.add(p3Field);

            centerPanel.add(new JLabel("TP 1 (15%):"));
            tp1Field = new JTextField(); centerPanel.add(tp1Field);

            centerPanel.add(new JLabel("TP 2 (15%):"));
            tp2Field = new JTextField(); centerPanel.add(tp2Field);

            centerPanel.add(new JLabel("Nota final:"));
            finalField = new JTextField();
            finalField.setEditable(false);
            centerPanel.add(finalField);

            JPanel bottomPanel = new JPanel(new BorderLayout());

            JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

            JButton borrarBtn = new JButton("Borrar Notas");
            borrarBtn.setBackground(new Color(255, 100, 100));
            borrarBtn.setForeground(Color.WHITE);
            borrarBtn.addActionListener(e -> borrarNotas());
            botonesPanel.add(borrarBtn);

            JButton guardarBtn = new JButton("Calcular y Guardar");
            guardarBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
            guardarBtn.addActionListener(e -> guardarNotas());
            botonesPanel.add(guardarBtn);

            bottomPanel.add(botonesPanel, BorderLayout.NORTH);

            MateriaNotas datosIniciales = getDatosMateriaAlumnoActual();
            graficoPanel = new GraficoNotasPanel(datosIniciales);
            graficoPanel.setBorder(BorderFactory.createTitledBorder("Gráfico de Rendimiento"));
            bottomPanel.add(graficoPanel, BorderLayout.CENTER);

            setLayout(new BorderLayout());
            add(topPanel, BorderLayout.NORTH);
            add(centerPanel, BorderLayout.CENTER);
            add(bottomPanel, BorderLayout.SOUTH);

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
            if (alumnosArray.length == 0) return;

            String[] nombres = new String[alumnosArray.length];
            for (int i = 0; i < alumnosArray.length; i++) {
                nombres[i] = alumnosArray[i].username + " - " + alumnosArray[i].nombre;
            }

            JList<String> lista = new JList<>(nombres);
            JScrollPane scrollPane = new JScrollPane(lista);

            JDialog dialog = new JDialog(this, "Seleccionar Alumno", true);
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
            botones.add(seleccionarBtn);
            dialog.add(botones, BorderLayout.SOUTH);
            dialog.setVisible(true);
        }

        private void mostrarTablaNotas() {
            if (alumnosArray.length == 0) return;

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
                data[i][7] = String.format("%.2f", mn.notaFinal);
            }

            JTable tabla = new JTable(data, columnas);
            JDialog dialog = new JDialog(this, "Planilla - " + materia, true);
            dialog.setSize(700, 300);
            dialog.setLocationRelativeTo(this);
            dialog.add(new JScrollPane(tabla));
            dialog.setVisible(true);
        }

        private void borrarNotas() {
            if (alumnoActual == null) return;

            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Estás seguro de borrar todas las notas de " + alumnoActual.nombre + " en " + materia + "?\nEsta acción no se puede deshacer.",
                    "Confirmar borrado", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                MateriaNotas mn = getDatosMateriaAlumnoActual();
                mn.parciales = new double[3];
                mn.tps = new double[2];
                mn.notaFinal = 0.0;

                NotasApp.guardarDatos();

                cargarDatosAlumnoActual();
                JOptionPane.showMessageDialog(this, "Notas eliminadas.");
            }
        }

        private void guardarNotas() {
            if (alumnoActual == null) return;
            try {
                MateriaNotas mn = getDatosMateriaAlumnoActual();

                mn.parciales[0] = validarNota(p1Field.getText());
                mn.parciales[1] = validarNota(p2Field.getText());
                mn.parciales[2] = validarNota(p3Field.getText());
                mn.tps[0] = validarNota(tp1Field.getText());
                mn.tps[1] = validarNota(tp2Field.getText());

                mn.notaFinal = calcularNotaFinal(mn);

                finalField.setText(String.format("%.2f", mn.notaFinal));
                graficoPanel.repaint();

                NotasApp.guardarDatos();

                JOptionPane.showMessageDialog(this,
                        "Notas guardadas para " + alumnoActual.nombre + ".",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error en los datos: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private double validarNota(String texto) throws NumberFormatException {
            double val = Double.parseDouble(texto);
            if (val < 0 || val > 10) {
                throw new NumberFormatException("La nota debe estar entre 0 y 10.");
            }
            return val;
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
            setSize(600, 450);
            setLocationRelativeTo(null);

            JPanel infoPanel = new JPanel(new GridLayout(6, 2, 5, 5));
            infoPanel.setBorder(BorderFactory.createTitledBorder("Mis Calificaciones"));

            infoPanel.add(new JLabel("Parcial 1:"));
            p1Label = new JLabel(); infoPanel.add(p1Label);

            infoPanel.add(new JLabel("Parcial 2:"));
            p2Label = new JLabel(); infoPanel.add(p2Label);

            infoPanel.add(new JLabel("Parcial 3:"));
            p3Label = new JLabel(); infoPanel.add(p3Label);

            infoPanel.add(new JLabel("TP 1:"));
            tp1Label = new JLabel(); infoPanel.add(tp1Label);

            infoPanel.add(new JLabel("TP 2:"));
            tp2Label = new JLabel(); infoPanel.add(tp2Label);

            infoPanel.add(new JLabel("Nota final:"));
            finalLabel = new JLabel();
            finalLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            infoPanel.add(finalLabel);

            MateriaNotas mn = student.notasPorMateria.get(materia);
            if (mn == null) {
                mn = new MateriaNotas();
                student.notasPorMateria.put(materia, mn);
            }
            graficoPanel = new GraficoNotasPanel(mn);

            JButton materiasBtn = new JButton("Volver a Materias");
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

            actualizarLabels(mn);
        }

        private void actualizarLabels(MateriaNotas mn) {
            p1Label.setText(String.valueOf(mn.parciales[0]));
            p2Label.setText(String.valueOf(mn.parciales[1]));
            p3Label.setText(String.valueOf(mn.parciales[2]));
            tp1Label.setText(String.valueOf(mn.tps[0]));
            tp2Label.setText(String.valueOf(mn.tps[1]));
            finalLabel.setText(String.format("%.2f", mn.notaFinal));
        }
    }
}