import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class FrenzieeApp {

    // ---------- DATABASE CONNECTION ----------
    static class DBConnection {
        private static final String URL = "jdbc:mysql://localhost:3306/frenziee";
        private static final String USER = "root";  // change if needed
        private static final String PASSWORD = "hudha7032397133";  // your MySQL password

        public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        }
    }

    // ---------- UI STYLE ----------
    static class UIStyle {
        static Color primary = new Color(76, 175, 80); // green
        static Color secondary = new Color(33, 150, 243); // blue
        static Color bg = new Color(245, 247, 250);
        static Color cardBg = new Color(255, 255, 255);
        static Font titleFont = new Font("Segoe UI", Font.BOLD, 28);
        static Font textFont = new Font("Segoe UI", Font.PLAIN, 14);

        static JButton styledButton(String text, Color color) {
            JButton btn = new JButton(text);
            btn.setBackground(color);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setOpaque(true);
            return btn;
        }

        static JPanel cardPanel() {
            JPanel panel = new JPanel();
            panel.setBackground(cardBg);
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            panel.setLayout(new BorderLayout());
            return panel;
        }
    }

    // ---------- LOGIN FRAME ----------
    static class LoginFrame extends JFrame {
        JTextField emailField;
        JPasswordField passwordField;
        JComboBox<String> roleBox;

        LoginFrame() {
            setTitle("Frenziee - Login");
            setSize(420, 400);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLayout(new BorderLayout(10, 10));
            getContentPane().setBackground(UIStyle.bg);

            JLabel title = new JLabel("✨ Frenziee Login ✨", SwingConstants.CENTER);
            title.setFont(UIStyle.titleFont);
            title.setForeground(UIStyle.secondary);
            add(title, BorderLayout.NORTH);

            JPanel formPanel = new JPanel(new GridLayout(6, 1, 10, 10));
            formPanel.setBorder(BorderFactory.createEmptyBorder(20, 60, 20, 60));
            formPanel.setBackground(UIStyle.bg);

            emailField = new JTextField();
            passwordField = new JPasswordField();
            roleBox = new JComboBox<>(new String[]{"user", "admin"});

            JButton loginBtn = UIStyle.styledButton("Login", UIStyle.secondary);
            JButton registerBtn = UIStyle.styledButton("Register", UIStyle.primary);

            formPanel.add(new JLabel("Email:"));
            formPanel.add(emailField);
            formPanel.add(new JLabel("Password:"));
            formPanel.add(passwordField);
            formPanel.add(new JLabel("Role:"));
            formPanel.add(roleBox);
            add(formPanel, BorderLayout.CENTER);

            JPanel btnPanel = new JPanel();
            btnPanel.setBackground(UIStyle.bg);
            btnPanel.add(loginBtn);
            btnPanel.add(registerBtn);
            add(btnPanel, BorderLayout.SOUTH);

            loginBtn.addActionListener(e -> loginUser());
            registerBtn.addActionListener(e -> {
                dispose();
                new RegisterFrame();
            });

            setVisible(true);
        }

        void loginUser() {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            String role = (String) roleBox.getSelectedItem();

            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement pst = con.prepareStatement(
                        "SELECT * FROM users WHERE email=? AND password=? AND role=?");
                pst.setString(1, email);
                pst.setString(2, password);
                pst.setString(3, role);
                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    int userId = rs.getInt("id");
                    JOptionPane.showMessageDialog(this, "Welcome " + rs.getString("name") + " (" + role + ")!");
                    dispose();
                    if (role.equalsIgnoreCase("admin"))
                        new AdminDashboard();
                    else
                        new UserDashboard(userId);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid email, password or role!");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // ---------- REGISTER FRAME ----------
    static class RegisterFrame extends JFrame {
        JTextField nameField, emailField;
        JPasswordField passwordField;
        JComboBox<String> roleBox;

        RegisterFrame() {
            setTitle("Frenziee - Register");
            setSize(420, 400);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLayout(new BorderLayout());
            getContentPane().setBackground(UIStyle.bg);

            JLabel title = new JLabel("Create an Account 🌸", SwingConstants.CENTER);
            title.setFont(UIStyle.titleFont);
            title.setForeground(UIStyle.primary);
            add(title, BorderLayout.NORTH);

            JPanel formPanel = new JPanel(new GridLayout(8, 1, 10, 10));
            formPanel.setBackground(UIStyle.bg);
            formPanel.setBorder(BorderFactory.createEmptyBorder(20, 60, 20, 60));

            nameField = new JTextField();
            emailField = new JTextField();
            passwordField = new JPasswordField();
            roleBox = new JComboBox<>(new String[]{"user", "admin"});

            JButton registerBtn = UIStyle.styledButton("Register", UIStyle.primary);
            JButton backBtn = UIStyle.styledButton("Back", UIStyle.secondary);

            formPanel.add(new JLabel("Name:"));
            formPanel.add(nameField);
            formPanel.add(new JLabel("Email:"));
            formPanel.add(emailField);
            formPanel.add(new JLabel("Password:"));
            formPanel.add(passwordField);
            formPanel.add(new JLabel("Role:"));
            formPanel.add(roleBox);

            JPanel btnPanel = new JPanel();
            btnPanel.setBackground(UIStyle.bg);
            btnPanel.add(registerBtn);
            btnPanel.add(backBtn);

            add(formPanel, BorderLayout.CENTER);
            add(btnPanel, BorderLayout.SOUTH);

            registerBtn.addActionListener(e -> registerUser());
            backBtn.addActionListener(e -> {
                dispose();
                new LoginFrame();
            });

            setVisible(true);
        }

        void registerUser() {
            String name = nameField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            String role = (String) roleBox.getSelectedItem();

            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO users(name,email,password,role) VALUES(?,?,?,?)");
                pst.setString(1, name);
                pst.setString(2, email);
                pst.setString(3, password);
                pst.setString(4, role);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Registration successful as " + role + "!");
                dispose();
                new LoginFrame();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    // ---------- USER DASHBOARD ----------
    static class UserDashboard extends JFrame {
        int userId;
        JTextArea postArea;
        JPanel postsPanel;

        UserDashboard(int userId) {
            this.userId = userId;
            setTitle("Frenziee - User Dashboard");
            setSize(700, 700);
            setLayout(new BorderLayout());
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            getContentPane().setBackground(UIStyle.bg);

            JLabel heading = new JLabel("Welcome to Frenziee Feed 🌼", SwingConstants.CENTER);
            heading.setFont(UIStyle.titleFont);
            heading.setForeground(UIStyle.secondary);
            add(heading, BorderLayout.NORTH);

            JPanel topPanel = new JPanel(new BorderLayout(10, 10));
            topPanel.setBackground(UIStyle.bg);

            postArea = new JTextArea(3, 40);
            postArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            JButton postBtn = UIStyle.styledButton("Post", UIStyle.primary);
            topPanel.add(new JScrollPane(postArea), BorderLayout.CENTER);
            topPanel.add(postBtn, BorderLayout.EAST);
            postBtn.addActionListener(e -> addPost());

            postsPanel = new JPanel();
            postsPanel.setBackground(UIStyle.bg);
            postsPanel.setLayout(new BoxLayout(postsPanel, BoxLayout.Y_AXIS));

            refreshPosts();

            add(topPanel, BorderLayout.SOUTH);
            add(new JScrollPane(postsPanel), BorderLayout.CENTER);
            setVisible(true);
        }

        void addPost() {
            String content = postArea.getText();
            if (content.isEmpty()) return;
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO posts(user_id, content) VALUES(?,?)");
                pst.setInt(1, userId);
                pst.setString(2, content);
                pst.executeUpdate();
                postArea.setText("");
                refreshPosts();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        void refreshPosts() {
            postsPanel.removeAll();
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement pst = con.prepareStatement(
                        "SELECT p.id, u.name, p.content, p.likes FROM posts p JOIN users u ON p.user_id=u.id ORDER BY p.id DESC");
                ResultSet rs = pst.executeQuery();
                while (rs.next()) {
                    int pid = rs.getInt("id");
                    String user = rs.getString("name");
                    String content = rs.getString("content");
                    int likes = rs.getInt("likes");

                    JPanel card = UIStyle.cardPanel();
                    card.setBorder(BorderFactory.createTitledBorder(user));
                    card.add(new JLabel("<html><p style='width:450px'>" + content + "</p></html>"), BorderLayout.CENTER);

                    JButton likeBtn = UIStyle.styledButton("❤️ " + likes, new Color(255, 87, 87));
                    JButton commentBtn = UIStyle.styledButton("💬 Comment", UIStyle.secondary);
                    JPanel btnPanel = new JPanel();
                    btnPanel.setBackground(Color.WHITE);
                    btnPanel.add(likeBtn);
                    btnPanel.add(commentBtn);
                    card.add(btnPanel, BorderLayout.SOUTH);

                    likeBtn.addActionListener(e -> {
                        try (Connection c = DBConnection.getConnection()) {
                            c.prepareStatement("UPDATE posts SET likes=likes+1 WHERE id=" + pid).executeUpdate();
                            refreshPosts();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    });

                    commentBtn.addActionListener(e -> showComments(pid));

                    postsPanel.add(Box.createVerticalStrut(10));
                    postsPanel.add(card);
                }
                postsPanel.revalidate();
                postsPanel.repaint();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        void showComments(int postId) {
            JDialog dialog = new JDialog(this, "Comments", true);
            dialog.setSize(400, 400);
            dialog.setLayout(new BorderLayout());
            dialog.getContentPane().setBackground(UIStyle.bg);

            JPanel commentPanel = new JPanel();
            commentPanel.setLayout(new BoxLayout(commentPanel, BoxLayout.Y_AXIS));
            commentPanel.setBackground(UIStyle.bg);

            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement pst = con.prepareStatement(
                        "SELECT u.name, c.comment FROM comments c JOIN users u ON c.user_id=u.id WHERE c.post_id=?");
                pst.setInt(1, postId);
                ResultSet rs = pst.executeQuery();
                while (rs.next()) {
                    commentPanel.add(new JLabel("<html><b>" + rs.getString("name") + ":</b> " + rs.getString("comment") + "</html>"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            JTextField newComment = new JTextField();
            JButton addCommentBtn = UIStyle.styledButton("Add", UIStyle.primary);
            addCommentBtn.addActionListener(e -> {
                String text = newComment.getText();
                if (!text.isEmpty()) {
                    try (Connection con = DBConnection.getConnection()) {
                        PreparedStatement pst = con.prepareStatement(
                                "INSERT INTO comments(post_id, user_id, comment) VALUES(?,?,?)");
                        pst.setInt(1, postId);
                        pst.setInt(2, userId);
                        pst.setString(3, text);
                        pst.executeUpdate();
                        dialog.dispose();
                        refreshPosts();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.add(newComment, BorderLayout.CENTER);
            bottomPanel.add(addCommentBtn, BorderLayout.EAST);

            dialog.add(new JScrollPane(commentPanel), BorderLayout.CENTER);
            dialog.add(bottomPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);
        }
    }

    // ---------- ADMIN DASHBOARD ----------
    static class AdminDashboard extends JFrame {
        JPanel postsPanel;

        AdminDashboard() {
            setTitle("Frenziee - Admin Dashboard");
            setSize(700, 700);
            setLayout(new BorderLayout());
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            getContentPane().setBackground(UIStyle.bg);

            JLabel heading = new JLabel("Admin Panel 🔧", SwingConstants.CENTER);
            heading.setFont(UIStyle.titleFont);
            heading.setForeground(UIStyle.secondary);
            add(heading, BorderLayout.NORTH);

            postsPanel = new JPanel();
            postsPanel.setLayout(new BoxLayout(postsPanel, BoxLayout.Y_AXIS));
            postsPanel.setBackground(UIStyle.bg);
            refreshPosts();

            add(new JScrollPane(postsPanel), BorderLayout.CENTER);
            setVisible(true);
        }

        void refreshPosts() {
            postsPanel.removeAll();
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement pst = con.prepareStatement(
                        "SELECT p.id, u.name, p.content, p.likes FROM posts p JOIN users u ON p.user_id=u.id ORDER BY p.id DESC");
                ResultSet rs = pst.executeQuery();
                while (rs.next()) {
                    int pid = rs.getInt("id");
                    String user = rs.getString("name");
                    String content = rs.getString("content");
                    int likes = rs.getInt("likes");

                    JPanel card = UIStyle.cardPanel();
                    card.setBorder(BorderFactory.createTitledBorder(user));
                    card.add(new JLabel("<html><p style='width:450px'>" + content + "</p></html>"), BorderLayout.CENTER);

                    JButton deleteBtn = UIStyle.styledButton("🗑 Delete", new Color(244, 67, 54));
                    JPanel btnPanel = new JPanel();
                    btnPanel.setBackground(Color.WHITE);
                    btnPanel.add(deleteBtn);
                    card.add(btnPanel, BorderLayout.SOUTH);

                    deleteBtn.addActionListener(e -> {
                        try (Connection c = DBConnection.getConnection()) {
                            c.prepareStatement("DELETE FROM posts WHERE id=" + pid).executeUpdate();
                            refreshPosts();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    });

                    postsPanel.add(Box.createVerticalStrut(10));
                    postsPanel.add(card);
                }
                postsPanel.revalidate();
                postsPanel.repaint();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ---------- MAIN ----------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
                      }
