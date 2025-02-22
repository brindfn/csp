;             
CREATE USER IF NOT EXISTS SA SALT '633340d183bd810b' HASH '41acd80f6ba5d2d22694a98582fcc3c5969af6e65d2e84a109b9b5feab387d4c' ADMIN;           
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_4962BA4D_B31E_428E_897E_6F97090277D2 START WITH 853 BELONGS_TO_TABLE;  
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_8074283F_B367_4F47_A686_7FD466119F14 START WITH 98 BELONGS_TO_TABLE;   
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_1E3E5A96_0379_470D_AA2D_43A52BA99B1E START WITH 160 BELONGS_TO_TABLE;  
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_C423F217_E4A3_4A03_A229_C2B4FC5F8E1B START WITH 34 BELONGS_TO_TABLE;   
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_89B494A5_0703_40CB_A4C8_D6650448FFC6 START WITH 541 BELONGS_TO_TABLE;  
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_2B50EB40_E8DF_487A_A148_8B3C13F8FA19 START WITH 121 BELONGS_TO_TABLE;  
CREATE SEQUENCE PUBLIC.SYSTEM_SEQUENCE_083FF303_5DEC_45ED_85E6_3F679EB5945A START WITH 66 BELONGS_TO_TABLE;   
CREATE CACHED TABLE PUBLIC.CSP(
    ID VARCHAR(255) NOT NULL,
    DOMAIN_NAME VARCHAR(255) NOT NULL,
    NAME VARCHAR(255) NOT NULL,
    REGISTRATION_DATE VARCHAR(255)
);               
ALTER TABLE PUBLIC.CSP ADD CONSTRAINT PUBLIC.CONSTRAINT_1 PRIMARY KEY(ID);    
-- 7 +/- SELECT COUNT(*) FROM PUBLIC.CSP;     
INSERT INTO PUBLIC.CSP(ID, DOMAIN_NAME, NAME, REGISTRATION_DATE) VALUES
('33333333-3333-3333-3333-333333333333', 'domain test 3', 'test3', '2017-09-06T19:09:06Z'),
('22222222-2222-2222-2222-222222222222', 'domain test 2', 'test2', '2017-09-06T19:10:17Z'),
('12345678-9123-4564-2665-5440000', 'dfncertmain.dfn.cert.org', 'DFN-Cert CSIRT Installation', '2017-09-06T21:44:16Z'),
('11111111-1111-1111-1111-111111111111', 'dfncertmain.dfn.cert.org', 'DFN-Cert CSIRT Installation1', '2017-03-30T23:59:50Z'),
('bc0f5efa-83ac-4d41-820d-52607dc31e95', 'dfncertmain.dfn.cert.org', 'DFN-Cert CSIRT Installation1', '2017-03-30T23:59:50Z'),
('dab91092-61d7-4fef-8e57-a0db3177193f', 'dfncertmain.dfn.cert.org', 'DFN-Cert CSIRT Installation1', '2017-03-30T23:59:50Z'),
('142ee3e0-c615-4fee-bdd2-c605ba9aa6aa', 'dfncertmain.dfn.cert.org', 'DFN-Cert CSIRT Installation1', '2017-03-30T23:59:50Z');        
CREATE CACHED TABLE PUBLIC.CSP_CONTACT(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_89B494A5_0703_40CB_A4C8_D6650448FFC6) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_89B494A5_0703_40CB_A4C8_D6650448FFC6,
    CONTACT_TYPE INTEGER NOT NULL,
    CSP_ID VARCHAR(255) NOT NULL,
    PERSON_EMAIL VARCHAR(255) NOT NULL,
    PERSON_NAME VARCHAR(255) NOT NULL
);         
ALTER TABLE PUBLIC.CSP_CONTACT ADD CONSTRAINT PUBLIC.CONSTRAINT_7 PRIMARY KEY(ID);            
-- 14 +/- SELECT COUNT(*) FROM PUBLIC.CSP_CONTACT;            
INSERT INTO PUBLIC.CSP_CONTACT(ID, CONTACT_TYPE, CSP_ID, PERSON_EMAIL, PERSON_NAME) VALUES
(5, 0, '33333333-3333-3333-3333-333333333333', 'email1@test3.test', 'test 3 name 1'),
(6, 1, '33333333-3333-3333-3333-333333333333', 'email2@test3.test', 'test 3 name 2'),
(7, 2, '22222222-2222-2222-2222-222222222222', 'email1@test2.test', 'test 2 name 1'),
(8, 1, '22222222-2222-2222-2222-222222222222', 'email2@test2.test', 'test 2 name 2'),
(453, 0, '12345678-9123-4564-2665-5440000', 'user@example.com', 'string'),
(454, 0, '12345678-9123-4564-2665-5440000', 'user@example.com', 'string'),
(509, 0, 'bc0f5efa-83ac-4d41-820d-52607dc31e95', 'user@example.com1', 'string1'),
(510, 0, 'bc0f5efa-83ac-4d41-820d-52607dc31e95', 'user@example.com2', 'string2'),
(521, 0, 'dab91092-61d7-4fef-8e57-a0db3177193f', 'user@example.com1', 'string1'),
(522, 0, 'dab91092-61d7-4fef-8e57-a0db3177193f', 'user@example.com2', 'string2'),
(533, 0, '142ee3e0-c615-4fee-bdd2-c605ba9aa6aa', 'user@example.com1', 'string1'),
(534, 0, '142ee3e0-c615-4fee-bdd2-c605ba9aa6aa', 'user@example.com2', 'string2'),
(539, 0, '11111111-1111-1111-1111-111111111111', 'user@example.com1', 'string1'),
(540, 0, '11111111-1111-1111-1111-111111111111', 'user@example.com2', 'string2');        
CREATE CACHED TABLE PUBLIC.CSP_INFO(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_1E3E5A96_0379_470D_AA2D_43A52BA99B1E) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_1E3E5A96_0379_470D_AA2D_43A52BA99B1E,
    CSP_ID VARCHAR(255) NOT NULL,
    RECORD_DATE_TIME VARCHAR(255) NOT NULL
);    
ALTER TABLE PUBLIC.CSP_INFO ADD CONSTRAINT PUBLIC.CONSTRAINT_E PRIMARY KEY(ID);               
-- 4 +/- SELECT COUNT(*) FROM PUBLIC.CSP_INFO;
INSERT INTO PUBLIC.CSP_INFO(ID, CSP_ID, RECORD_DATE_TIME) VALUES
(86, 'bc0f5efa-83ac-4d41-820d-52607dc31e95', '2017-09-06T21:49:27Z'),
(88, 'dab91092-61d7-4fef-8e57-a0db3177193f', '2017-09-06T22:16:43Z'),
(90, '142ee3e0-c615-4fee-bdd2-c605ba9aa6aa', '2017-09-06T22:17:54Z'),
(159, '11111111-1111-1111-1111-111111111111', '2017-09-06T22:29:10Z'); 
CREATE CACHED TABLE PUBLIC.CSP_IP(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_4962BA4D_B31E_428E_897E_6F97090277D2) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_4962BA4D_B31E_428E_897E_6F97090277D2,
    CSP_ID VARCHAR(255) NOT NULL,
    EXTERNAL INTEGER NOT NULL,
    IP VARCHAR(255) NOT NULL
);    
ALTER TABLE PUBLIC.CSP_IP ADD CONSTRAINT PUBLIC.CONSTRAINT_77 PRIMARY KEY(ID);
-- 22 +/- SELECT COUNT(*) FROM PUBLIC.CSP_IP; 
INSERT INTO PUBLIC.CSP_IP(ID, CSP_ID, EXTERNAL, IP) VALUES
(5, '33333333-3333-3333-3333-333333333333', 0, '333.333.333.333'),
(6, '33333333-3333-3333-3333-333333333333', 1, '333.333.333.333'),
(7, '22222222-2222-2222-2222-222222222222', 0, '222.222.222.222'),
(8, '22222222-2222-2222-2222-222222222222', 1, '222.222.222.222'),
(709, '12345678-9123-4564-2665-5440000', 0, '192.168.1.1'),
(710, '12345678-9123-4564-2665-5440000', 1, '150.140.187.1'),
(789, 'bc0f5efa-83ac-4d41-820d-52607dc31e95', 1, '150.140.187.1'),
(790, 'bc0f5efa-83ac-4d41-820d-52607dc31e95', 1, '150.140.187.2'),
(791, 'bc0f5efa-83ac-4d41-820d-52607dc31e95', 0, '192.168.1.1'),
(792, 'bc0f5efa-83ac-4d41-820d-52607dc31e95', 0, '192.168.1.2'),
(813, 'dab91092-61d7-4fef-8e57-a0db3177193f', 1, '150.140.187.1'),
(814, 'dab91092-61d7-4fef-8e57-a0db3177193f', 1, '150.140.187.2'),
(815, 'dab91092-61d7-4fef-8e57-a0db3177193f', 0, '192.168.1.1'),
(816, 'dab91092-61d7-4fef-8e57-a0db3177193f', 0, '192.168.1.2'),
(837, '142ee3e0-c615-4fee-bdd2-c605ba9aa6aa', 1, '150.140.187.1'),
(838, '142ee3e0-c615-4fee-bdd2-c605ba9aa6aa', 1, '150.140.187.2'),
(839, '142ee3e0-c615-4fee-bdd2-c605ba9aa6aa', 0, '192.168.1.1'),
(840, '142ee3e0-c615-4fee-bdd2-c605ba9aa6aa', 0, '192.168.1.2'),
(849, '11111111-1111-1111-1111-111111111111', 1, '150.140.187.1'),
(850, '11111111-1111-1111-1111-111111111111', 1, '150.140.187.2'),
(851, '11111111-1111-1111-1111-111111111111', 0, '192.168.1.1'),
(852, '11111111-1111-1111-1111-111111111111', 0, '192.168.1.2');        
CREATE CACHED TABLE PUBLIC.CSP_MANAGEMENT(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_8074283F_B367_4F47_A686_7FD466119F14) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_8074283F_B367_4F47_A686_7FD466119F14,
    CSP_ID VARCHAR(255) NOT NULL,
    DATE_CHANGED VARCHAR(255),
    MODULE_ID BIGINT NOT NULL,
    MODULE_VERSION_ID BIGINT NOT NULL
);   
ALTER TABLE PUBLIC.CSP_MANAGEMENT ADD CONSTRAINT PUBLIC.CONSTRAINT_C PRIMARY KEY(ID);         
-- 3 +/- SELECT COUNT(*) FROM PUBLIC.CSP_MANAGEMENT;          
INSERT INTO PUBLIC.CSP_MANAGEMENT(ID, CSP_ID, DATE_CHANGED, MODULE_ID, MODULE_VERSION_ID) VALUES
(2, '33333333-3333-3333-3333-333333333333', '2017-09-06T19:10:34Z', 2, 34),
(66, '11111111-1111-1111-1111-111111111111', '2017-09-06T21:47:09Z', 1, 1),
(67, '11111111-1111-1111-1111-111111111111', '2017-09-06T21:47:09Z', 2, 34);      
CREATE CACHED TABLE PUBLIC.CSP_MODULE_INFO(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_2B50EB40_E8DF_487A_A148_8B3C13F8FA19) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_2B50EB40_E8DF_487A_A148_8B3C13F8FA19,
    CSP_INFO_ID BIGINT NOT NULL,
    MODULE_INSTALLED_ON VARCHAR(255) NOT NULL,
    MODULE_IS_ACTIVE INTEGER NOT NULL,
    MODULE_VERSION_ID BIGINT NOT NULL
);           
ALTER TABLE PUBLIC.CSP_MODULE_INFO ADD CONSTRAINT PUBLIC.CONSTRAINT_5 PRIMARY KEY(ID);        
-- 4 +/- SELECT COUNT(*) FROM PUBLIC.CSP_MODULE_INFO;         
INSERT INTO PUBLIC.CSP_MODULE_INFO(ID, CSP_INFO_ID, MODULE_INSTALLED_ON, MODULE_IS_ACTIVE, MODULE_VERSION_ID) VALUES
(48, 86, '2017-04-24T11:59:35Z', 0, 1),
(50, 88, '2017-04-24T11:59:35Z', 0, 1),
(52, 90, '2017-04-24T11:59:35Z', 0, 1),
(120, 159, '2017-04-24T11:59:35Z', 1, 1);    
CREATE CACHED TABLE PUBLIC.MODULE(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_C423F217_E4A3_4A03_A229_C2B4FC5F8E1B) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_C423F217_E4A3_4A03_A229_C2B4FC5F8E1B,
    IS_DEFAULT INTEGER NOT NULL,
    NAME VARCHAR(255) NOT NULL,
    START_PRIORITY INTEGER NOT NULL
);             
ALTER TABLE PUBLIC.MODULE ADD CONSTRAINT PUBLIC.CONSTRAINT_8 PRIMARY KEY(ID); 
-- 2 +/- SELECT COUNT(*) FROM PUBLIC.MODULE;  
INSERT INTO PUBLIC.MODULE(ID, IS_DEFAULT, NAME, START_PRIORITY) VALUES
(1, 0, 'module1', 1),
(2, 1, 'module2', 2);          
CREATE CACHED TABLE PUBLIC.MODULE_VERSION(
    ID BIGINT DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_083FF303_5DEC_45ED_85E6_3F679EB5945A) NOT NULL NULL_TO_DEFAULT SEQUENCE PUBLIC.SYSTEM_SEQUENCE_083FF303_5DEC_45ED_85E6_3F679EB5945A,
    DESCRIPTION VARCHAR(255),
    FULL_NAME VARCHAR(255) NOT NULL,
    HASH VARCHAR(255) NOT NULL,
    MODULE_ID BIGINT NOT NULL,
    RELEASED_ON VARCHAR(255) NOT NULL,
    VERSION INTEGER NOT NULL
); 
ALTER TABLE PUBLIC.MODULE_VERSION ADD CONSTRAINT PUBLIC.CONSTRAINT_4 PRIMARY KEY(ID);         
-- 3 +/- SELECT COUNT(*) FROM PUBLIC.MODULE_VERSION;          
INSERT INTO PUBLIC.MODULE_VERSION(ID, DESCRIPTION, FULL_NAME, HASH, MODULE_ID, RELEASED_ON, VERSION) VALUES
(1, 'module1.1', 'module1.1', 'fd61127757973c982cec9d15b61da61a173c8ea86c3122655b92abacec5c7edacff933f896a895d50ecfa3c8f9fc34eb76258bbc228fdf35a5b767a9b1a4c9', 1, '2017-09-06T18:05:06Z', 10000),
(2, 'module1.2', 'module1.2', 'f723468e86eeeac25c74e6f63b3a4d931a8599b2f7814f36fc66049518cdfbe9dc697baa44c517270f839e6d563eb19e142e96e31a29550a1aa4837b9a95d', 1, '2017-09-06T18:17:23Z', 20000),
(34, 'module2.1', 'module2.1', 'cdd9f8683877d46e7a98c591433d2792a76919d1041ccf9d5da66ceb40aa97ac6b48af40cd39b5544c8724951f570223dd1a90a5d8abfd87467ad3c7d833', 2, '2017-09-06T19:09:55Z', 10000);         
ALTER TABLE PUBLIC.MODULE ADD CONSTRAINT PUBLIC.UK_F73DSVAOR0F4CYCVLDYT2IDF1 UNIQUE(NAME);    
ALTER TABLE PUBLIC.MODULE_VERSION ADD CONSTRAINT PUBLIC.UK_EPUC9SKUK4MCRJ90T64Q6N25C UNIQUE(HASH);            
