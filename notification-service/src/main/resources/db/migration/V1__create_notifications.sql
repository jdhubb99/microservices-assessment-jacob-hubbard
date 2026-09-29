CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    event_id UUID NOT NULL,
    order_id UUID NOT NULL,
    type VARCHAR(40) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    recipient VARCHAR(320) NOT NULL,
    message TEXT NOT NULL,
    order_status VARCHAR(20) NOT NULL,
    sent_at TIMESTAMPTZ NOT NULL
);