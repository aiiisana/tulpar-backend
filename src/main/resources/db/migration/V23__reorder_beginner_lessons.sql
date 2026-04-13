-- V32: Move conversational lessons (V31) to positions 1-5 in BEGINNER level
--      and shift the original lessons (Алфавит, Сандар, …) to positions 6-10.
--
-- BEGINNER level id: 3f98ca3c-d431-480f-87d5-b5d43996d9e4
--
-- Step 1: push old lessons to 6-10 (use large temp values first to avoid
--         unique-constraint collisions if the column is indexed)

UPDATE lessons SET order_index = order_index + 100
WHERE level_id = '3f98ca3c-d431-480f-87d5-b5d43996d9e4'
  AND id IN (
    'bd97e322-b609-4d54-b6e6-920256478014',  -- Алфавит   (was 1)
    '25c9b7f0-a89d-4392-a4b5-8cdcea3129c2',  -- Сандар    (was 2)
    '9bba9cdd-89aa-49b1-90db-06206729c4b5',  -- Амандасу  (was 3)
    'd220650c-e656-4a3b-b31e-5fbd17e755fc',  -- Түстер    (was 4)
    '1d969bba-2a4e-4377-92f8-ab56b3d114f9'   -- Жануарлар (was 5)
  );

-- Step 2: move conversational lessons to positions 1-5
UPDATE lessons SET order_index = order_index - 5
WHERE level_id = '3f98ca3c-d431-480f-87d5-b5d43996d9e4'
  AND id IN (
    'cafe0006-0006-4006-8006-b00000000006',  -- Амандасу және сыпайылық (was 6  → 1)
    'cafe0007-0007-4007-8007-b00000000007',  -- Өзіңді таныстыру        (was 7  → 2)
    'cafe0008-0008-4008-8008-b00000000008',  -- Сұрақтар мен жауаптар   (was 8  → 3)
    'cafe0009-0009-4009-8009-b00000000009',  -- Күнделікті лексика      (was 9  → 4)
    'cafe0010-0010-4010-8010-b00000000010'   -- Қарапайым сөйлесу       (was 10 → 5)
  );

-- Step 3: settle old lessons to 6-10 (remove the +100 offset)
UPDATE lessons SET order_index = order_index - 100
WHERE level_id = '3f98ca3c-d431-480f-87d5-b5d43996d9e4'
  AND id IN (
    'bd97e322-b609-4d54-b6e6-920256478014',  -- Алфавит   → 6
    '25c9b7f0-a89d-4392-a4b5-8cdcea3129c2',  -- Сандар    → 7
    '9bba9cdd-89aa-49b1-90db-06206729c4b5',  -- Амандасу  → 8
    'd220650c-e656-4a3b-b31e-5fbd17e755fc',  -- Түстер    → 9
    '1d969bba-2a4e-4377-92f8-ab56b3d114f9'   -- Жануарлар → 10
  );
