UPDATE stock SET 
	quantity = COALESCE(:quantity, stock.quantity), 
	reserved_quantity = COALESCE(:reservedQuantity, stock.reserved_quantity) 
WHERE item_id = :id