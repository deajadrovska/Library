-- init.sql
CREATE TABLE IF NOT EXISTS country (
                                       id SERIAL PRIMARY KEY,
                                       name VARCHAR(255) NOT NULL,
    continent VARCHAR(255) NOT NULL
    );

CREATE TABLE IF NOT EXISTS author (
                                      id SERIAL PRIMARY KEY,
                                      name VARCHAR(255) NOT NULL,
    surname VARCHAR(255) NOT NULL,
    country_id BIGINT NOT NULL,
    CONSTRAINT fk_country FOREIGN KEY (country_id) REFERENCES country(id)
    );

CREATE TABLE IF NOT EXISTS book (
                                    id SERIAL PRIMARY KEY,
                                    name VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    author_id BIGINT NOT NULL,
    available_copies INTEGER NOT NULL,
    CONSTRAINT fk_author FOREIGN KEY (author_id) REFERENCES author(id)
    );

CREATE TABLE IF NOT EXISTS library_users (
                                             username VARCHAR(255) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    surname VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_account_non_expired BOOLEAN DEFAULT TRUE,
    is_account_non_locked BOOLEAN DEFAULT TRUE,
    is_credentials_non_expired BOOLEAN DEFAULT TRUE,
    is_enabled BOOLEAN DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS wishlist (
                                        id SERIAL PRIMARY KEY,
                                        user_username VARCHAR(255) NOT NULL,
    date_created TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_username) REFERENCES library_users(username)
    );

CREATE TABLE IF NOT EXISTS wishlist_books (
                                              wishlist_id BIGINT NOT NULL,
                                              books_id BIGINT NOT NULL,
                                              PRIMARY KEY (wishlist_id, books_id),
    CONSTRAINT fk_wishlist FOREIGN KEY (wishlist_id) REFERENCES wishlist(id),
    CONSTRAINT fk_book FOREIGN KEY (books_id) REFERENCES book(id)
    );

-- Create indexes for better performance
CREATE INDEX idx_book_author ON book(author_id);
CREATE INDEX idx_author_country ON author(country_id);
CREATE INDEX idx_wishlist_user ON wishlist(user_username);
CREATE INDEX idx_user_role ON library_users(role);