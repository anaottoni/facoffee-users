--migration para inserir as roles padrões usando IDs do tipo long
INSERT INTO roles (id, name) 
VALUES 
    (1, 'PARTICIPANT'),
    (2, 'MANAGER')
ON CONFLICT (name) DO NOTHING;