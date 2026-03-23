UPDATE public.retry_events
SET success = :success, retries = :retries
WHERE id = :id