CREATE TABLE IF NOT EXISTS Roles (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS Users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password TEXT NOT NULL,
    role_id INT REFERENCES Roles(role_id) ON DELETE SET NULL,
    email VARCHAR(50) UNIQUE,
    telephone VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS Employees (
    employee_id SERIAL PRIMARY KEY,
    user_id INT UNIQUE REFERENCES Users(user_id) ON DELETE CASCADE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    hired_at DATE NOT NULL,
    salary NUMERIC(10, 2) NOT NULL CHECK (salary > 0)
);

CREATE TABLE IF NOT EXISTS Shifts (
    shift_id SERIAL PRIMARY KEY,
    employee_id INT REFERENCES Employees(employee_id) ON DELETE CASCADE,
    shift_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    CONSTRAINT unique_shift UNIQUE (employee_id, shift_date, start_time, end_time)
);

CREATE TABLE IF NOT EXISTS Menu_Items (
    item_id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    price NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    available BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS Orders (
    order_id SERIAL PRIMARY KEY,
    user_id INT REFERENCES Users(user_id) ON DELETE SET NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_price NUMERIC(10, 2) NOT NULL CHECK (total_price >= 0)
);

CREATE TABLE IF NOT EXISTS Order_History (
    order_history_id SERIAL PRIMARY KEY,
    order_id INT REFERENCES Orders(order_id) ON DELETE CASCADE,
    time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT unique_order_history UNIQUE (order_id, time, status)
);

CREATE TABLE IF NOT EXISTS Order_Items (
    order_item_id SERIAL PRIMARY KEY,
    order_id INT REFERENCES Orders(order_id) ON DELETE CASCADE,
    item_id INT REFERENCES Menu_Items(item_id) ON DELETE CASCADE,
    quantity INT NOT NULL CHECK (quantity > 0),
    CONSTRAINT unique_order_item UNIQUE (order_id, item_id)
);

CREATE TABLE IF NOT EXISTS Reviews (
    review_id SERIAL PRIMARY KEY,
    user_id INT REFERENCES Users(user_id) ON DELETE SET NULL,
    order_id INT REFERENCES Orders(order_id) ON DELETE CASCADE,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_review UNIQUE (user_id, order_id) -- Уникальное ограничение для одного отзыва на заказ от одного пользователя
);
