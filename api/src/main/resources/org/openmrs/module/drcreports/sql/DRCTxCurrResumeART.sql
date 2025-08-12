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
      AND c_question.uuid = '1255AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' -- Decision on ART during this visit
      AND c_answer.uuid = '162904AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' -- Resume ART
      
      AND DATE(o.obs_datetime) >= :onOrAfter
      AND DATE(o.obs_datetime) <= :onOrBefore
  )
ORDER BY p.patient_id;
