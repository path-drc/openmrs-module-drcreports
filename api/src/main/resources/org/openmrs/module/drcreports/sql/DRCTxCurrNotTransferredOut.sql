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
      AND DATE(o.value_datetime) BETWEEN :onOrAfter AND :onOrBefore
      AND NOT EXISTS (
        -- Must have transferred back in after this transfer out in the same reporting period
        SELECT 1
        FROM obs o_in
        INNER JOIN concept c_question_in ON o_in.concept_id = c_question_in.concept_id
        INNER JOIN concept c_answer_in ON o_in.value_coded = c_answer_in.concept_id
        WHERE o_in.person_id = p.patient_id
          AND o_in.voided = 0
          AND c_question_in.uuid = '83e40f2c-c316-43e6-a12e-20a338100281' -- What do you want to do?
          AND c_answer_in.uuid = '160563AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' -- Transfer in
          AND DATE(o_in.obs_datetime) BETWEEN :onOrAfter AND :onOrBefore
          AND o_in.obs_datetime > o.value_datetime -- Transfer in must be after transfer out
          AND EXISTS (
            SELECT 1
            FROM obs o_regimen
            INNER JOIN concept c_regimen ON o_regimen.concept_id = c_regimen.concept_id
            WHERE o_regimen.encounter_id = o_in.encounter_id
              AND o_regimen.voided = 0
              AND c_regimen.uuid = 'dfbe256e-30ba-4033-837a-2e8477f2e7cd' -- ART Regimen
              AND o_regimen.value_coded IS NOT NULL
          )
      )
  )
ORDER BY p.patient_id;