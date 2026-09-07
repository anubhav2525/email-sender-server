-- ════════════════════════════════════════════════════
-- STEP 2: updated_at Auto-Update Function
-- ════════════════════════════════════════════════════

CREATE
OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at
= now();
RETURN NEW;
END;
$$
LANGUAGE plpgsql;