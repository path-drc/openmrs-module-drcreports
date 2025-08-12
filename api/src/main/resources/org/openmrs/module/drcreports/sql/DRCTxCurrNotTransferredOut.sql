SELECT DISTINCT p.patient_id
FROM patient p
INNER JOIN person pe ON p.patient_id = pe.person_id
WHERE pe.voided = 0 
  AND p.voided = 0
  AND NOT EXISTS (
    SELECT 1
    FROM obs o
    INNER JOIN concept c ON o.concept_id = c.concept_id
    WHERE o.person_id = p.patient_id
      AND o.voided = 0
      AND c.uuid = '160649AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' -- Date Transferred
      
      AND DATE(o.value_datetime) >= :onOrAfter
      AND DATE(o.value_datetime) <= :onOrBefore
  )
ORDER BY p.patient_id;