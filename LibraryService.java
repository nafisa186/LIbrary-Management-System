
package com.library.service;

import com.library.dao.BookDAO;
import com.library.dao.IssueDAO;
import com.library.dao.MemberDAO;
import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.model.BookIssue;
import com.library.model.Member;

import java.sql.SQLException;
import java.util.List;

public class LibraryService {

    private final BookDAO bookDAO = new BookDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final IssueDAO issueDAO = new IssueDAO();

    public void addBook(Book book) throws LibraryException {
        try {
            bookDAO.add(book);
        } catch (SQLException ex) {
            throw new LibraryException("Unable to add book.", ex);
        }
    }

    public List<Book> getBooks() throws LibraryException {
        try {
            return bookDAO.findAll();
        } catch (SQLException ex) {
            throw new LibraryException("Unable to load books.", ex);
        }
    }

    public List<Book> searchBooks(String keyword)
            throws LibraryException {
        try {
            return bookDAO.search(keyword);
        } catch (SQLException ex) {
            throw new LibraryException("Book search failed.", ex);
        }
    }

    public void updateBook(Book book) throws LibraryException {
        try {
            bookDAO.update(book);
        } catch (SQLException ex) {
            throw new LibraryException("Unable to update book.", ex);
        }
    }

    public void deleteBook(int id) throws LibraryException {
        try {
            bookDAO.delete(id);
        } catch (SQLException ex) {
            throw new LibraryException(
                    "Unable to delete book. It may have issue history.", ex);
        }
    }

    public void addMember(Member member) throws LibraryException {
        try {
            memberDAO.add(member);
        } catch (SQLException ex) {
            throw new LibraryException("Unable to register member.", ex);
        }
    }

    public List<Member> getMembers() throws LibraryException {
        try {
            return memberDAO.findAll();
        } catch (SQLException ex) {
            throw new LibraryException("Unable to load members.", ex);
        }
    }

    public void issueBook(int bookId, int memberId, int loanDays)
            throws LibraryException {
        try {
            issueDAO.issueBook(bookId, memberId, loanDays);
        } catch (SQLException | IllegalArgumentException ex) {
            throw new LibraryException("Book issue failed: "
                    + ex.getMessage(), ex);
        }
    }

    public void returnBook(int issueId) throws LibraryException {
        try {
            issueDAO.returnBook(issueId);
        } catch (SQLException ex) {
            throw new LibraryException("Book return failed: "
                    + ex.getMessage(), ex);
        }
    }

    public List<BookIssue> getIssues() throws LibraryException {
        try {
            return issueDAO.findAll();
        } catch (SQLException ex) {
            throw new LibraryException("Unable to load issue records.", ex);
        }
    }
}
