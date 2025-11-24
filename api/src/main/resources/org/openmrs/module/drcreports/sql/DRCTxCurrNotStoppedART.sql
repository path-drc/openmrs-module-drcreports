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
      AND DATE(o_date.value_datetime) BETWEEN :onOrAfter AND :onOrBefore
      AND NOT EXISTS (
        -- Must have resumed ART after this stop in the same reporting period
        SELECT 1
        FROM obs o_resume
        INNER JOIN concept c_question_resume ON o_resume.concept_id = c_question_resume.concept_id
        INNER JOIN concept c_answer_resume ON o_resume.value_coded = c_answer_resume.concept_id
        WHERE o_resume.person_id = p.patient_id
          AND o_resume.voided = 0
          AND c_question_resume.uuid = '1255AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' -- Decision on ART during this visit
          AND c_answer_resume.uuid = '162904AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' -- Resume ART
          AND DATE(o_resume.obs_datetime) BETWEEN :onOrAfter AND :onOrBefore
          AND o_resume.obs_datetime > o_date.value_datetime
      )
  )
ORDER BY p.patient_id;