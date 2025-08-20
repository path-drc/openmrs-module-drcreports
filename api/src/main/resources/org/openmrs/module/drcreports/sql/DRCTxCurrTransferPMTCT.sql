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
      AND c_question.uuid = '83e40f2c-c316-43e6-a12e-20a338100281' -- What do you want to do?
      AND c_answer.uuid IN (
        '160563AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA', -- Transfer in
        '163532AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'  -- Enroll into PMTCT
      )
      AND DATE(o.obs_datetime) >= :onOrAfter
      AND DATE(o.obs_datetime) <= :onOrBefore
      AND (
        -- If answer is Transfer In, must have ART Regimen in same encounter
        (c_answer.uuid = '160563AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
         AND EXISTS (
           SELECT 1
           FROM obs o_regimen
           INNER JOIN concept c_regimen ON o_regimen.concept_id = c_regimen.concept_id
           WHERE o_regimen.encounter_id = o.encounter_id
             AND o_regimen.voided = 0
             AND c_regimen.uuid = 'dfbe256e-30ba-4033-837a-2e8477f2e7cd' -- ART Regimen
             AND o_regimen.value_coded IS NOT NULL
         )
        )
        OR
        -- If answer is PMTCT, must have Prevention being taken in same encounter
        (c_answer.uuid = '163532AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
         AND EXISTS (
           SELECT 1
           FROM obs o_prevention
           INNER JOIN concept c_prevention ON o_prevention.concept_id = c_prevention.concept_id
           INNER JOIN concept c_prevention_value ON o_prevention.value_coded = c_prevention_value.concept_id
           WHERE o_prevention.encounter_id = o.encounter_id
             AND o_prevention.voided = 0
             AND c_prevention.uuid = '163532AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' -- ART Prevention being taken
             AND c_prevention_value.uuid = '1065AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' -- YES
         )
        )
      )
  )
ORDER BY p.patient_id;