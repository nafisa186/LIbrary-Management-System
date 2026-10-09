
package com.library.ui;

import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.model.BookIssue;
import com.library.model.Member;
import com.library.service.LibraryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Dashboard extends JFrame {

    private final LibraryService service = new LibraryService();

    private final DefaultTableModel bookModel = new DefaultTableModel(
            new String[]{"ID", "Title", "Author", "ISBN", "Total", "Available"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) { return false; }
    };

    private final DefaultTableModel memberModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Email", "Phone"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) { return false; }
    };

    private final DefaultTableModel issueModel = new DefaultTableModel(
            new String[]{"Issue ID", "Book ID", "Member ID",
                    "Issue Date", "Due Date", "Return Date", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) { return false; }
    };

    private final JTable bookTable = new JTable(bookModel);
    private final JTable memberTable = new JTable(memberModel);
    private final JTable issueTable = new JTable(issueModel);

    public Dashboard() {
        setTitle("Library Management System");
        setSize(1050, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Books", createBooksPanel());
        tabs.addTab("Members", createMembersPanel());
        tabs.addTab("Issue / Return", createIssuesPanel());

        add(tabs);
        refreshAll();
    }

    private JPanel createBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JTextField search = new JTextField(18);
        JButton searchButton = new JButton("Search");
        JButton allButton = new JButton("Show All");
        JButton addButton = new JButton("Add Book");
        JButton updateButton = new JButton("Update Selected");
        JButton deleteButton = new JButton("Delete Selected");
        JButton refreshButton = new JButton("Refresh");

        actions.add(new JLabel("Search:"));
        actions.add(search);
        actions.add(searchButton);
        actions.add(allButton);
        actions.add(addButton);
        actions.add(updateButton);
        actions.add(deleteButton);
        actions.add(refreshButton);

        searchButton.addActionListener(e ->
                runTask(() -> service.searchBooks(search.getText().trim()),
                        this::showBooks));

        allButton.addActionListener(e ->
                runTask(service::getBooks, this::showBooks));

        refreshButton.addActionListener(e ->
                runTask(service::getBooks, this::showBooks));

        addButton.addActionListener(e -> addBook());
        updateButton.addActionListener(e -> updateSelectedBook());
        deleteButton.addActionListener(e -> deleteSelectedBook());

        panel.add(actions, BorderLayout.NORTH);
        panel.add(new JScrollPane(bookTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMembersPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel actions = new JPanel();

        JButton add = new JButton("Register Member");
        JButton refresh = new JButton("Refresh Members");

        add.addActionListener(e -> addMember());
        refresh.addActionListener(e ->
                runTask(service::getMembers, this::showMembers));

        actions.add(add);
        actions.add(refresh);

        panel.add(actions, BorderLayout.NORTH);
        panel.add(new JScrollPane(memberTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createIssuesPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel actions = new JPanel();

        JButton issue = new JButton("Issue Book");
        JButton returnBook = new JButton("Return Selected Issue");
        JButton refresh = new JButton("Refresh Issues");

        issue.addActionListener(e -> issueBook());
        returnBook.addActionListener(e -> returnSelectedBook());
        refresh.addActionListener(e ->
                runTask(service::getIssues, this::showIssues));

        actions.add(issue);
        actions.add(returnBook);
        actions.add(refresh);

        panel.add(actions, BorderLayout.NORTH);
        panel.add(new JScrollPane(issueTable), BorderLayout.CENTER);
        return panel;
    }

    @FunctionalInterface
    private interface Task<T> {
        T execute() throws Exception;
    }

    private <T> void runTask(Task<T> task,
                             java.util.function.Consumer<T> success) {
        SwingWorker<T, Void> worker = new SwingWorker<>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.execute();
            }

            @Override
            protected void done() {
                try {
                    success.accept(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError("Operation interrupted.");
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    showError(cause.getMessage() == null
                            ? "Operation failed." : cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void addBook() {
        JTextField title = new JTextField();
        JTextField author = new JTextField();
        JTextField isbn = new JTextField();
        JTextField copies = new JTextField("1");

        Object[] fields = {
                "Title:", title,
                "Author:", author,
                "ISBN (optional):", isbn,
                "Total copies:", copies
        };

        if (JOptionPane.showConfirmDialog(this, fields, "Add Book",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String t = title.getText().trim();
            String a = author.getText().trim();
            String i = isbn.getText().trim();
            int count = Integer.parseInt(copies.getText().trim());

            if (t.isEmpty() || a.isEmpty() || count < 0) {
                throw new IllegalArgumentException(
                        "Enter a title, author and valid copy count.");
            }

            Book book = new Book(0, t, a,
                    i.isEmpty() ? null : i, count, count);

            runTask(() -> {
                service.addBook(book);
                return service.getBooks();
            }, books -> {
                showBooks(books);
                showMessage("Book added successfully.");
            });
        } catch (NumberFormatException | IllegalArgumentException ex) {
            showError("Enter a valid non-negative copy count and book details.");
        }
    }

    private void updateSelectedBook() {
        int row = bookTable.getSelectedRow();
        if (row < 0) {
            showError("Select a book first.");
            return;
        }

        int id = (int) bookModel.getValueAt(row, 0);
        String title = bookModel.getValueAt(row, 1).toString();
        String author = bookModel.getValueAt(row, 2).toString();
        Object isbnValue = bookModel.getValueAt(row, 3);
        String isbn = isbnValue == null ? "" : isbnValue.toString();
        String copies = bookModel.getValueAt(row, 4).toString();

        JTextField t = new JTextField(title);
        JTextField a = new JTextField(author);
        JTextField i = new JTextField(isbn);
        JTextField c = new JTextField(copies);

        Object[] fields = {
                "Title:", t, "Author:", a, "ISBN:", i, "Total copies:", c
        };

        if (JOptionPane.showConfirmDialog(this, fields, "Update Book",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            int total = Integer.parseInt(c.getText().trim());
            if (t.getText().isBlank() || a.getText().isBlank() || total < 0) {
                throw new IllegalArgumentException("Invalid book details.");
            }

            Book updated = new Book(id, t.getText().trim(),
                    a.getText().trim(),
                    i.getText().isBlank() ? null : i.getText().trim(),
                    total, 0);

            runTask(() -> {
                // Reload the latest available count before updating.
                Book current = service.getBooks().stream()
                        .filter(b -> b.getId() == id)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Book not found."));

                if (total < current.getAvailableCopies()) {
                    throw new IllegalArgumentException(
                            "Total copies cannot be less than available copies.");
                }

                Book result = new Book(id, updated.getTitle(),
                        updated.getAuthor(), updated.getIsbn(),
                        total, current.getAvailableCopies());

                service.updateBook(result);
                return service.getBooks();
            }, books -> {
                showBooks(books);
                showMessage("Book updated successfully.");
            });
        } catch (NumberFormatException | IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void deleteSelectedBook() {
        int row = bookTable.getSelectedRow();
        if (row < 0) {
            showError("Select a book first.");
            return;
        }

        int id = (int) bookModel.getValueAt(row, 0);

        if (JOptionPane.showConfirmDialog(this,
                "Delete this book?", "Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        runTask(() -> {
            service.deleteBook(id);
            return service.getBooks();
        }, books -> {
            showBooks(books);
            showMessage("Book deleted.");
        });
    }

    private void addMember() {
        JTextField name = new JTextField();
        JTextField email = new JTextField();
        JTextField phone = new JTextField();

        Object[] fields = {
                "Name:", name,
                "Email (optional):", email,
                "Phone:", phone
        };

        if (JOptionPane.showConfirmDialog(this, fields, "Register Member",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }

        String n = name.getText().trim();
        String em = email.getText().trim();
        String ph = phone.getText().trim();

        if (n.isEmpty()) {
            showError("Member name is required.");
            return;
        }

        Member member = new Member(0, n,
                em.isEmpty() ? null : em,
                ph.isEmpty() ? null : ph);

        runTask(() -> {
            service.addMember(member);
            return service.getMembers();
        }, members -> {
            showMembers(members);
            showMessage("Member registered.");
        });
    }

    private void issueBook() {
        JTextField bookId = new JTextField();
        JTextField memberId = new JTextField();
        JTextField days = new JTextField("14");

        Object[] fields = {
                "Book ID:", bookId,
                "Member ID:", memberId,
                "Loan days:", days
        };

        if (JOptionPane.showConfirmDialog(this, fields, "Issue Book",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            int b = Integer.parseInt(bookId.getText().trim());
            int m = Integer.parseInt(memberId.getText().trim());
            int d = Integer.parseInt(days.getText().trim());

            if (b <= 0 || m <= 0 || d <= 0) {
                throw new IllegalArgumentException(
                        "IDs and loan days must be positive.");
            }

            runTask(() -> {
                service.issueBook(b, m, d);
                return service.getIssues();
            }, issues -> {
                showIssues(issues);
                refreshBooks();
                showMessage("Book issued successfully.");
            });
        } catch (NumberFormatException | IllegalArgumentException ex) {
            showError("Enter valid positive numeric IDs and loan days.");
        }
    }

    private void returnSelectedBook() {
        int row = issueTable.getSelectedRow();
        if (row < 0) {
            showError("Select an issue record first.");
            return;
        }

        int issueId = (int) issueModel.getValueAt(row, 0);

        runTask(() -> {
            service.returnBook(issueId);
            return service.getIssues();
        }, issues -> {
            showIssues(issues);
            refreshBooks();
            showMessage("Book returned successfully.");
        });
    }

    private void refreshBooks() {
        runTask(service::getBooks, this::showBooks);
    }

    private void refreshAll() {
        refreshBooks();
        runTask(service::getMembers, this::showMembers);
        runTask(service::getIssues, this::showIssues);
    }

    private void showBooks(List<Book> books) {
        bookModel.setRowCount(0);
        for (Book b : books) {
            bookModel.addRow(new Object[]{
                    b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(),
                    b.getTotalCopies(), b.getAvailableCopies()
            });
        }
    }

    private void showMembers(List<Member> members) {
        memberModel.setRowCount(0);
        for (Member m : members) {
            memberModel.addRow(new Object[]{
                    m.getId(), m.getName(), m.getEmail(), m.getPhone()
            });
        }
    }

    private void showIssues(List<BookIssue> issues) {
        issueModel.setRowCount(0);
        for (BookIssue i : issues) {
            issueModel.addRow(new Object[]{
                    i.getId(), i.getBookId(), i.getMemberId(),
                    i.getIssueDate(), i.getDueDate(),
                    i.getReturnDate(), i.getStatus()
            });
        }
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
