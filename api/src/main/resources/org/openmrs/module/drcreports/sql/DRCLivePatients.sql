SELECT DISTINCT p.patient_id
FROM patient p
INNER JOIN person pe ON p.patient_id = pe.person_id
WHERE pe.voided = 0 
  AND p.voided = 0
  AND NOT EXISTS (
    -- Check that patient does NOT have death date observation
    SELECT 1
    FROM obs o
    INNER JOIN concept c ON o.concept_id = c.concept_id
    WHERE o.person_id = p.patient_id
      AND o.voided = 0
      AND c.uuid = '1543AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
  )
ORDER BY p.patient_id;