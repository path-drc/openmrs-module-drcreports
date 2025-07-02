SELECT 
    p.patient_id,
    CONCAT_WS(' ', 
        COALESCE(pn.given_name, ''),
        COALESCE(pn.middle_name, ''),
        COALESCE(pn.family_name, '')
    ) AS `Nom_et_postnom_du_patient`,
    identifier.identifier AS `Code_TAR`,
    person.gender AS gender,
    DATE_FORMAT(person.birthdate, '%d/%m/%Y') AS birthdate,
    TIMESTAMPDIFF(YEAR, person.birthdate, CURDATE()) AS `Age`,
    CONCAT_WS(' ', 
        COALESCE(address.city_village, ''),
        COALESCE(address.address1, ''),
        COALESCE(address.address2, ''),
        COALESCE(address.state_province, ''),
        COALESCE(address.country, '')
    ) AS `Provenance_du_patient`,
    MAX(CASE WHEN c.uuid = '5497AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' THEN o.value_numeric END) AS `Numeration_CD4`,
        MAX(CASE WHEN c.uuid = '5497AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' THEN 
        DATE_FORMAT(o.obs_datetime, '%d/%m/%Y')
    END) AS `Numeration_CD4_Date_realise`
FROM 
    patient p
    INNER JOIN person_name pn ON p.patient_id = pn.person_id
    LEFT JOIN person person ON p.patient_id = person.person_id
    LEFT JOIN obs o ON p.patient_id = o.person_id
    LEFT JOIN concept c ON o.concept_id = c.concept_id
    LEFT JOIN person_address address ON person.person_id = address.person_id
    LEFT JOIN patient_identifier identifier ON p.patient_id = identifier.patient_id
    LEFT JOIN concept_name cn ON c.concept_id = cn.concept_id AND cn.concept_name_type = 'FULLY_SPECIFIED'
    LEFT JOIN concept coded_c ON o.value_coded = coded_c.concept_id
    LEFT JOIN concept_name coded_cn ON coded_c.concept_id = coded_cn.concept_id 
        AND coded_cn.concept_name_type = 'FULLY_SPECIFIED'
        AND coded_cn.locale = 'en'
WHERE 
    (c.uuid IN ('5497AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') OR c.uuid IS NULL)
    AND pn.voided = 0
    AND p.voided = 0
    AND o.voided = 0
    AND o.obs_datetime BETWEEN :startDate AND :endDate

GROUP BY 
    p.patient_id, 
    CONCAT_WS(' ', 
        COALESCE(pn.given_name, ''),
        COALESCE(pn.middle_name, ''),
        COALESCE(pn.family_name, '')
    ),
    identifier.identifier,
    person.gender,
    person.birthdate,
    CONCAT_WS(' ', 
        COALESCE(address.city_village, ''),
        COALESCE(address.address1, ''),
        COALESCE(address.address2, ''),
        COALESCE(address.state_province, ''),
        COALESCE(address.country, '')
    )
ORDER BY 
    p.patient_id;