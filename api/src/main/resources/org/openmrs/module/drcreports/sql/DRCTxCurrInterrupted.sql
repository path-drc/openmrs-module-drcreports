SELECT DISTINCT p.patient_id
FROM patient p
INNER JOIN person pe ON p.patient_id = pe.person_id
WHERE pe.voided = 0 
  AND p.voided = 0
  AND EXISTS (
    -- Filter for patients who have refill dates in the reporting period
    SELECT 1 
    FROM obs o_appt
    INNER JOIN concept c_appt ON o_appt.concept_id = c_appt.concept_id
    WHERE o_appt.person_id = p.patient_id
      AND o_appt.voided = 0
      AND c_appt.uuid = '162549AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' -- Next Refill Date concept
      AND DATE(o_appt.value_datetime) >= :onOrAfter
      AND DATE(o_appt.value_datetime) <= :onOrBefore
  )
  AND NOT EXISTS (
    -- Exclude patients who meet the compliance criteria based on their latest refill date
    SELECT 1 
    FROM obs o_appt
    INNER JOIN concept c_appt ON o_appt.concept_id = c_appt.concept_id
    WHERE o_appt.person_id = p.patient_id
      AND o_appt.voided = 0
      AND c_appt.uuid = '162549AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' -- Next Refill Date concept
      AND DATE(o_appt.value_datetime) >= :onOrAfter
      AND DATE(o_appt.value_datetime) <= :onOrBefore
      -- Ensure this is the latest refill date for this patient in the date range
      AND o_appt.value_datetime = (
        SELECT MAX(o_latest.value_datetime)
        FROM obs o_latest
        INNER JOIN concept c_latest ON o_latest.concept_id = c_latest.concept_id
        WHERE o_latest.person_id = p.patient_id
          AND o_latest.voided = 0
          AND c_latest.uuid = '162549AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
          AND DATE(o_latest.value_datetime) >= :onOrAfter
          AND DATE(o_latest.value_datetime) <= :onOrBefore
      )
      AND (
        -- If latest appointment is within 28 days of end date, this would be compliant
        DATE(o_appt.value_datetime) > DATE_SUB(:onOrBefore, INTERVAL 28 DAY)
        OR
        -- If latest appointment is more than 28 days before end date, and has subsequent visit, this would be compliant
        (
          DATE(o_appt.value_datetime) <= DATE_SUB(:onOrBefore, INTERVAL 28 DAY)
          AND EXISTS (
            SELECT 1
            FROM encounter e_subsequent
            INNER JOIN encounter_type et_subsequent ON e_subsequent.encounter_type = et_subsequent.encounter_type_id
            INNER JOIN obs o_arv_subsequent ON o_arv_subsequent.encounter_id = e_subsequent.encounter_id
            INNER JOIN concept c_arv_subsequent ON o_arv_subsequent.concept_id = c_arv_subsequent.concept_id
            WHERE e_subsequent.patient_id = p.patient_id
              AND e_subsequent.voided = 0
              AND o_arv_subsequent.voided = 0
              AND et_subsequent.uuid = 'cb0a65a7-0587-477e-89b9-cf2fd144f1d4' -- Consultation encounter type
              AND c_arv_subsequent.uuid = '3a0709e9-d7a8-44b9-9512-111db5ce3989' -- ARV quantity concept
              AND o_arv_subsequent.value_numeric IS NOT NULL
              AND o_arv_subsequent.value_numeric > 0
              AND DATE(e_subsequent.encounter_datetime) > DATE(o_appt.value_datetime)
              AND DATE(e_subsequent.encounter_datetime) <= :onOrBefore
          )
        )
      )
  )
ORDER BY p.patient_id;