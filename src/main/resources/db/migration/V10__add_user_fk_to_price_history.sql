ALTER TABLE product_price_history
ADD CONSTRAINT fk_price_hist_user
FOREIGN KEY (changed_by_user_id) REFERENCES users (id) ON DELETE RESTRICT;
