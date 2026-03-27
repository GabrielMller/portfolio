SELECT id, status, created_at, user_id, total, order_number 
  FROM orders 
 WHERE id = :id