CREATE TABLE traders (
    id VARCHAR(10) PRIMARY KEY
);

CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trader_id VARCHAR(10) NOT NULL,
    stock VARCHAR(10) NOT NULL,
    sector VARCHAR(20) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    side VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    version INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_orders_trader FOREIGN KEY (trader_id) REFERENCES traders(id)
);

CREATE TABLE portfolio (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trader_id VARCHAR(10) NOT NULL,
    stock VARCHAR(10) NOT NULL,
    sector VARCHAR(20) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    version INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_portfolio_trader_stock UNIQUE (trader_id, stock),
    CONSTRAINT fk_portfolio_trader FOREIGN KEY (trader_id) REFERENCES traders(id)
);
