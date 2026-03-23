SELECT * FROM public.retry_events
WHERE success = :success
AND retries < :max_retries
AND event_type = 'ORDER'