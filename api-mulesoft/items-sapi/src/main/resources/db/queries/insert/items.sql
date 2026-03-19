INSERT INTO public.items (id, name, description, price, image, sku, created_at, updated_at) VALUES
(:id, :name, :description, :price, :image, :sku, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)