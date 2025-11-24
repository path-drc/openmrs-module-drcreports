SELECT DISTINCT p.patient_id
FROM patient p
INNER JOIN person pe ON p.patient_id = pe.person_id
WHERE pe.voided = 0 
  AND p.voided = 0
  AND EXISTS (
    SELECT 1
    FROM obs o
    INNER JOIN concept c ON o.concept_id = c.concept_id
    WHERE o.person_id = p.patient_id
      AND o.voided = 0
      AND c.uuid = '3a0709e9-d7a8-44b9-9512-111db5ce3989'
      AND o.value_numeric IS NOT NULL
      AND o.value_numeric >= 90
      AND o.value_numeric <= 179
      AND o.obs_datetime = (
        SELECT MAX(o2.obs_datetime)
        FROM obs o2
        INNER JOIN concept c2 ON o2.concept_id = c2.concept_id
        WHERE o2.person_id = o.person_id
          AND o2.voided = 0
          AND c2.uuid = '3a0709e9-d7a8-44b9-9512-111db5ce3989'
          AND o2.value_numeric IS NOT NULL
      )
  )
ORDER BY p.patient_id;