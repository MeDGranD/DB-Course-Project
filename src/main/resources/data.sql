INSERT INTO Roles (role_name)
VALUES
    ('Manager'),
    ('Employee'),
    ('Customer')
ON CONFLICT (role_name) DO NOTHING;

INSERT INTO Menu_Items (name, description, price, available)
VALUES
    ('Burger', 'Juicy grilled burger', 5.99, TRUE),
    ('Pizza', 'Cheesy pizza with toppings', 8.99, TRUE),
    ('Pasta', 'Delicious pasta with sauce', 7.49, TRUE)
ON CONFLICT (name) DO NOTHING;