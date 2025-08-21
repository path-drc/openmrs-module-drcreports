SELECT DISTINCT p.patient_id
FROM patient p
INNER JOIN person pe ON p.patient_id = pe.person_id
WHERE pe.voided = 0 
  AND p.voided = 0
  AND EXISTS (
    SELECT 1
    FROM obs o
    INNER JOIN concept c_question ON o.concept_id = c_question.concept_id
    INNER JOIN concept c_answer ON o.value_coded = c_answer.concept_id
    WHERE o.person_id = p.patient_id
      AND o.voided = 0
      AND c_question.uuid = '1255AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' -- ART initiation during this visit
      AND c_answer.uuid = '1256AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' -- Start ART
      AND DATE(o.obs_datetime) BETWEEN :onOrAfter AND :onOrBefore
  )
  AND EXISTS (
    -- Check for initiation date concept within date range
    SELECT 1
    FROM obs o_init_date
    INNER JOIN concept c_init_date ON o_init_date.concept_id = c_init_date.concept_id
    WHERE o_init_date.person_id = p.patient_id
      AND o_init_date.voided = 0
      AND c_init_date.uuid = '159599AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' -- Initiation date concept
      AND DATE(o_init_date.value_datetime) BETWEEN :onOrAfter AND :onOrBefore
  )
ORDER BY p.patient_id;
