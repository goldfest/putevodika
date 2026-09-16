ALTER TABLE category
    ADD COLUMN preference_selectable BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE category
SET preference_selectable = TRUE
WHERE code IN (
               'MUSEUM',
               'MONUMENT',
               'ARCHITECTURE',
               'NATURE',
               'PARK',
               'VIEWPOINT',
               'RELIGIOUS',
               'CULTURE'
    );