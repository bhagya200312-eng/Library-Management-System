package librarymanagementsystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class LibraryUI extends JFrame {

    private JTextField txtId;
    private JTextField txtTitle;
    private JTextField txtAuthor;
    private JTextField txtIsbn;
    private JCheckBox chkAvailable;
    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtSearch;
    private JComboBox<String> cmbSearchField;
    private JComboBox<String> cmbFilterAvailability;
    private JLabel lblStatus;

    private BookDAO bookDAO;

    public LibraryUI() {
        bookDAO = new BookDAO();
        initComponents();
        loadTableData();
    }

    private void initComponents() {
        setTitle("Simple Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // ---------- Menu Bar ----------
        JMenuBar menuBar = new JMenuBar();

        JMenu menuFile = new JMenu("File");
        JMenuItem menuExit = new JMenuItem("Exit");
        menuExit.addActionListener(e -> System.exit(0));
        menuFile.add(menuExit);

        JMenu menuHelp = new JMenu("Help");
        JMenuItem menuAbout = new JMenuItem("About");
        menuAbout.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "Simple Library Management System\nDeveloped in Java Swing + MySQL",
                "About",
                JOptionPane.INFORMATION_MESSAGE
        ));
        menuHelp.add(menuAbout);

        menuBar.add(menuFile);
        menuBar.add(menuHelp);

        setJMenuBar(menuBar);

        // ---------- Form Panel ----------
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Book Details"));

        formPanel.add(new JLabel("ID:"));
        txtId = new JTextField();
        txtId.setEditable(false);
        formPanel.add(txtId);

        formPanel.add(new JLabel("Title:"));
        txtTitle = new JTextField();
        formPanel.add(txtTitle);

        formPanel.add(new JLabel("Author:"));
        txtAuthor = new JTextField();
        formPanel.add(txtAuthor);

        formPanel.add(new JLabel("ISBN:"));
        txtIsbn = new JTextField();
        formPanel.add(txtIsbn);

        formPanel.add(new JLabel("Available:"));
        chkAvailable = new JCheckBox("Yes");
        chkAvailable.setSelected(true);
        formPanel.add(chkAvailable);

        // ---------- Search + Filter Panel ----------
        JPanel searchPanel = new JPanel();
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search & Filter"));

        searchPanel.add(new JLabel("Search:"));
        txtSearch = new JTextField(15);
        searchPanel.add(txtSearch);

        cmbSearchField = new JComboBox<>(new String[]{"Title", "Author", "ISBN"});
        searchPanel.add(cmbSearchField);

        JButton btnSearch = new JButton("Search");
        JButton btnShowAll = new JButton("Show All");
        searchPanel.add(btnSearch);
        searchPanel.add(btnShowAll);

        searchPanel.add(new JLabel("  Filter:"));
        cmbFilterAvailability = new JComboBox<>(new String[]{"All", "Available", "Borrowed"});
        searchPanel.add(cmbFilterAvailability);

        // ---------- Button Panel ----------
        JPanel buttonPanel = new JPanel();

        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        // ---------- Table ----------
        tableModel = new DefaultTableModel(new Object[]{"ID", "Title", "Author", "ISBN", "Available"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // All cells not editable directly
            }
        };
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true); // allow sorting by clicking headers
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Book List"));

        // ---------- Status Bar ----------
        lblStatus = new JLabel("Ready");
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        statusPanel.add(lblStatus, BorderLayout.WEST);

        // ---------- Layout ----------
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // ---------- Button Actions ----------
        btnAdd.addActionListener(e -> addBook());
        btnUpdate.addActionListener(e -> updateBook());
        btnDelete.addActionListener(e -> deleteBook());
        btnClear.addActionListener(e -> clearForm());

        btnSearch.addActionListener(e -> searchBooks());
        btnShowAll.addActionListener(e -> loadTableData());
        cmbFilterAvailability.addActionListener(e -> applyAvailabilityFilter());

        // When clicking a row in the table, load that row into the form
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    int modelRow = table.convertRowIndexToModel(selectedRow);

                    txtId.setText(tableModel.getValueAt(modelRow, 0).toString());
                    txtTitle.setText(tableModel.getValueAt(modelRow, 1).toString());
                    txtAuthor.setText(tableModel.getValueAt(modelRow, 2).toString());
                    txtIsbn.setText(tableModel.getValueAt(modelRow, 3).toString());
                    String availText = tableModel.getValueAt(modelRow, 4).toString();
                    chkAvailable.setSelected(availText.equalsIgnoreCase("Yes"));
                }
            }
        });
    }

    // ---------- Data Loading ----------
    private void loadTableData() {
        List<Book> books = bookDAO.getAllBooks();
        loadTableData(books);
        updateStatus("Loaded all books.", books.size());
        cmbFilterAvailability.setSelectedIndex(0); // reset filter
    }

    private void loadTableData(List<Book> books) {
        tableModel.setRowCount(0); // clear table
        for (Book b : books) {
            tableModel.addRow(new Object[]{
                    b.getId(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getIsbn(),
                    b.isAvailable() ? "Yes" : "No"
            });
        }
    }

    // ---------- CRUD Logic ----------
    private void addBook() {
        String title = txtTitle.getText().trim();
        String author = txtAuthor.getText().trim();
        String isbn = txtIsbn.getText().trim();
        boolean available = chkAvailable.isSelected();

        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields (except ID).");
            return;
        }

        // Check duplicate ISBN
        if (bookDAO.isIsbnExists(isbn, null)) {
            JOptionPane.showMessageDialog(this, "A book with this ISBN already exists.");
            return;
        }

        Book book = new Book(title, author, isbn, available);
        boolean success = bookDAO.insertBook(book);
        if (success) {
            JOptionPane.showMessageDialog(this, "Book added successfully.");
            clearForm();
            loadTableData();
        } else {
            JOptionPane.showMessageDialog(this, "Error adding book.");
        }
    }

    private void updateBook() {
        if (txtId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a book from the table first.");
            return;
        }

        int id = Integer.parseInt(txtId.getText().trim());
        String title = txtTitle.getText().trim();
        String author = txtAuthor.getText().trim();
        String isbn = txtIsbn.getText().trim();
        boolean available = chkAvailable.isSelected();

        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields (except ID).");
            return;
        }

        // Check duplicate ISBN, excluding this ID
        if (bookDAO.isIsbnExists(isbn, id)) {
            JOptionPane.showMessageDialog(this, "Another book with this ISBN already exists.");
            return;
        }

        Book book = new Book(id, title, author, isbn, available);
        boolean success = bookDAO.updateBook(book);
        if (success) {
            JOptionPane.showMessageDialog(this, "Book updated successfully.");
            clearForm();
            loadTableData();
        } else {
            JOptionPane.showMessageDialog(this, "Error updating book.");
        }
    }

    private void deleteBook() {
        if (txtId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a book from the table first.");
            return;
        }

        int id = Integer.parseInt(txtId.getText().trim());
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this book?");
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = bookDAO.deleteBook(id);
            if (success) {
                JOptionPane.showMessageDialog(this, "Book deleted successfully.");
                clearForm();
                loadTableData();
            } else {
                JOptionPane.showMessageDialog(this, "Error deleting book.");
            }
        }
    }

    private void clearForm() {
        txtId.setText("");
        txtTitle.setText("");
        txtAuthor.setText("");
        txtIsbn.setText("");
        chkAvailable.setSelected(true);
        txtSearch.setText("");
        cmbSearchField.setSelectedIndex(0);
        cmbFilterAvailability.setSelectedIndex(0);
        updateStatus("Form cleared.", tableModel.getRowCount());
    }

    // ---------- Search ----------
    private void searchBooks() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a search term.");
            return;
        }

        String field = (String) cmbSearchField.getSelectedItem();
        List<Book> result = bookDAO.searchBooks(keyword, field);

        if (result.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No books found.");
        }

        loadTableData(result);
        updateStatus("Search for \"" + keyword + "\" in " + field + ".", result.size());
    }

    // ---------- Availability Filter ----------
    private void applyAvailabilityFilter() {
        String selection = (String) cmbFilterAvailability.getSelectedItem();
        if (selection == null) return;

        if (selection.equals("All")) {
            loadTableData();
            return;
        }

        boolean available = selection.equals("Available");
        List<Book> books = bookDAO.getBooksByAvailability(available);
        loadTableData(books);
        updateStatus("Filter applied: " + selection + ".", books.size());
    }

    // ---------- Status Bar Helper ----------
    private void updateStatus(String message, int count) {
        lblStatus.setText("Total books: " + count + " | " + message);
    }

    // ---------- Main ----------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LibraryUI().setVisible(true);
        });
    }
}
