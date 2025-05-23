CREATE TABLE fish_backup AS SELECT * FROM fish;
ALTER TABLE fish
    ADD COLUMN description TEXT DEFAULT NULL;
UPDATE fish
SET description = 'Немає опису'
WHERE description IS NULL;