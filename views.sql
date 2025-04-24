-- views.sql
-- Materialized view for books count by author
CREATE MATERIALIZED VIEW IF NOT EXISTS books_by_author AS
SELECT a.id as author_id,
       a.name,
       a.surname,
       COUNT(b.id) as book_count
FROM author a
         LEFT JOIN book b ON a.id = b.author_id
GROUP BY a.id, a.name, a.surname
    WITH DATA;

-- Create index for better performance
CREATE UNIQUE INDEX IF NOT EXISTS idx_books_by_author ON books_by_author (author_id);

-- Materialized view for authors count by country
CREATE MATERIALIZED VIEW IF NOT EXISTS authors_by_country AS
SELECT c.id as country_id,
       c.name as country_name,
       COUNT(a.id) as author_count
FROM country c
         LEFT JOIN author a ON c.id = a.country_id
GROUP BY c.id, c.name
    WITH DATA;

-- Create index for better performance
CREATE UNIQUE INDEX IF NOT EXISTS idx_authors_by_country ON authors_by_country (country_id);

-- Function to refresh the materialized view
CREATE OR REPLACE FUNCTION refresh_authors_by_country()
RETURNS TRIGGER AS $$
BEGIN
    REFRESH MATERIALIZED VIEW authors_by_country;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Triggers to refresh the view when authors are modified
CREATE OR REPLACE TRIGGER refresh_authors_by_country_insert
AFTER INSERT ON author
FOR EACH STATEMENT
EXECUTE FUNCTION refresh_authors_by_country();

CREATE OR REPLACE TRIGGER refresh_authors_by_country_update
AFTER UPDATE ON author
                           FOR EACH STATEMENT
                           EXECUTE FUNCTION refresh_authors_by_country();

CREATE OR REPLACE TRIGGER refresh_authors_by_country_delete
AFTER DELETE ON author
FOR EACH STATEMENT
EXECUTE FUNCTION refresh_authors_by_country();