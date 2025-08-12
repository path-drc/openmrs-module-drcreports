SELECT DISTINCT p.patient_id
FROM patient p
INNER JOIN person pe ON p.patient_id = pe.person_id
WHERE pe.voided = 0 
  AND p.voided = 0
  AND NOT EXISTS (
    SELECT 1
    FROM obs o
    INNER JOIN concept c_question ON o.concept_id = c_question.concept_id
    INNER JOIN concept c_answer ON o.value_coded = c_answer.concept_id
    INNER JOIN obs o_date ON o.encounter_id = o_date.encounter_id 
    INNER JOIN concept c_date ON o_date.concept_id = c_date.concept_id
    WHERE o.person_id = p.patient_id
      AND o.voided = 0
      AND o_date.voided = 0
      AND c_question.uuid = '1255AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' -- Decision on ART during this visit
      AND c_answer.uuid = '1260AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' -- Stopped ART
      AND c_date.uuid = '162572AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' -- Stop Date
      
      AND DATE(o_date.value_datetime) >= :onOrAfter
      AND DATE(o_date.value_datetime) <= :onOrBefore
  )
ORDER BY p.patient_id;