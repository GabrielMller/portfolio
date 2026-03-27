SELECT id, status, payment_method, amount, created_at 
  FROM payments 
 WHERE order_id = :id