INSERT INTO public.retry_events (data, event_type, reason, event_id)
VALUES (:data, 'ORDER', :reason, :event_id)