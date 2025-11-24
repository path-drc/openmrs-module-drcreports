SELECT DISTINCT p.patient_id
FROM patient p
INNER JOIN person pe ON p.patient_id = pe.person_id
WHERE pe.voided = 0 
  AND p.voided = 0
  AND EXISTS (
    -- Check for "Next Refill Date" within or beyond date range
    SELECT 1 
    FROM obs o_appt
    INNER JOIN concept c_appt ON o_appt.concept_id = c_appt.concept_id
    WHERE o_appt.person_id = p.patient_id
      AND o_appt.voided = 0
      AND c_appt.uuid = '162549AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
      AND DATE(o_appt.value_datetime) >= :onOrAfter
  )
  AND EXISTS (
    -- Check ARV coverage: Either drugs cover entire range OR drugs end in range with appointment/visit logic
    SELECT 1
    FROM encounter e_consult
    INNER JOIN encounter_type et ON e_consult.encounter_type = et.encounter_type_id
    INNER JOIN obs o_arv ON o_arv.encounter_id = e_consult.encounter_id
    INNER JOIN concept c_arv ON o_arv.concept_id = c_arv.concept_id
    WHERE e_consult.patient_id = p.patient_id
      AND e_consult.voided = 0
      AND o_arv.voided = 0
      AND et.uuid = 'cb0a65a7-0587-477e-89b9-cf2fd144f1d4'
      AND c_arv.uuid = '3a0709e9-d7a8-44b9-9512-111db5ce3989'
      AND DATE(e_consult.encounter_datetime) <= :onOrBefore
      AND (
        -- Either: ARV quantity covers the entire date range
        DATE_ADD(DATE(e_consult.encounter_datetime), INTERVAL o_arv.value_numeric DAY) >= :onOrBefore
        OR
        -- Or: Drugs end within date range with appointment/visit logic based on timing
        (
          DATE_ADD(DATE(e_consult.encounter_datetime), INTERVAL o_arv.value_numeric DAY) < :onOrBefore
          AND DATE_ADD(DATE(e_consult.encounter_datetime), INTERVAL o_arv.value_numeric DAY) >= :onOrAfter
          AND EXISTS (
            SELECT 1
            FROM obs o_appt
            INNER JOIN concept c_appt ON o_appt.concept_id = c_appt.concept_id
            WHERE o_appt.person_id = p.patient_id
              AND o_appt.voided = 0
              AND c_appt.uuid = '162549AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
              AND (
                -- If appointment is within 28 days of end date, just check it exists
                (DATE(o_appt.value_datetime) > DATE_SUB(:onOrBefore, INTERVAL 28 DAY))
                OR
                -- If appointment is 28+ days before end date, must have subsequent visit WITH drug dispensing
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
                      AND et_subsequent.uuid = 'cb0a65a7-0587-477e-89b9-cf2fd144f1d4'
                      AND c_arv_subsequent.uuid = '3a0709e9-d7a8-44b9-9512-111db5ce3989'
                      AND o_arv_subsequent.value_numeric IS NOT NULL
                      AND o_arv_subsequent.value_numeric > 0
                      AND DATE(e_subsequent.encounter_datetime) > DATE(o_appt.value_datetime)
                      AND DATE(e_subsequent.encounter_datetime) <= :onOrBefore
                  )
                )
              )
          )
        )
      )
  )
ORDER BY p.patient_id;