-- Composite index for frequent filter: active tours by category (GET /tours, GET /tours/category/:id)
CREATE INDEX IF NOT EXISTS idx_tours_active_category ON tours(active, category_id);

-- Composite for guide + active (GET /tours/guide/:id, my-tours)
CREATE INDEX IF NOT EXISTS idx_tours_guide_active ON tours(guide_id, active);
