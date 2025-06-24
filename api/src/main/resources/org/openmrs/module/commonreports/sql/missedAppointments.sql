SELECT DISTINCT
    p.patient_id,
    pn.given_name,
    pn.family_name,
    per.gender,
    per.birthdate,
    o.value_datetime AS missed_date,
    'Return visit date' AS source
FROM patient p
JOIN obs o ON p.patient_id = o.person_id
JOIN person per ON p.patient_id = per.person_id
JOIN person_name pn ON per.person_id = pn.person_id
WHERE o.concept_id = 211
  AND o.voided = 0
  AND p.voided = 0
  AND o.value_datetime BETWEEN :startDate AND :endDate
  AND pn.preferred = 1

UNION

SELECT DISTINCT
    pa.patient_id,
    pn.given_name,
    pn.family_name,
    per.gender,
    per.birthdate,
    pa.start_date_time AS missed_date,
    'Appointment' AS source
FROM patient_appointment pa
JOIN patient p ON pa.patient_id = p.patient_id
JOIN person per ON p.patient_id = per.person_id
JOIN person_name pn ON per.person_id = pn.person_id
WHERE pa.start_date_time BETWEEN :startDate AND :endDate
  AND pa.voided = 0
  AND p.voided = 0
  AND pn.preferred = 1;