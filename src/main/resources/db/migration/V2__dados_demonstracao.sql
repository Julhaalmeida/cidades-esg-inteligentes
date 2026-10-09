-- =============================================================================
-- V2 - Dados de DEMONSTRAÇÃO
--
-- ATENÇÃO: os valores dos indicadores são ILUSTRATIVOS, criados apenas para
-- demonstrar a API, o cálculo do score e o ranking. Não são estatísticas
-- oficiais das cidades. A população é aproximada.
-- =============================================================================

INSERT INTO cidade (nome, uf, populacao) VALUES
    ('Curitiba', 'PR', 1773000),
    ('Florianópolis', 'SC', 537000),
    ('Belo Horizonte', 'MG', 2316000),
    ('São Paulo', 'SP', 11452000),
    ('Recife', 'PE', 1489000),
    ('Belém', 'PA', 1303000);

-- medições de 2025 para todos os indicadores + algumas de 2024 (histórico)
INSERT INTO indicador_esg (cidade_id, tipo, valor, data_referencia, fonte)
SELECT c.id, v.tipo, v.valor, v.data_referencia, 'Dados ilustrativos (demonstração)'
FROM (VALUES
    ('Curitiba',       'PR', 'EMISSOES_CO2_PER_CAPITA',     1.5, DATE '2025-12-31'),
    ('Curitiba',       'PR', 'ENERGIA_RENOVAVEL',            92, DATE '2025-12-31'),
    ('Curitiba',       'PR', 'RECICLAGEM_RESIDUOS',          32, DATE '2025-12-31'),
    ('Curitiba',       'PR', 'COBERTURA_SANEAMENTO',         98, DATE '2025-12-31'),
    ('Curitiba',       'PR', 'ACESSO_TRANSPORTE_PUBLICO',    82, DATE '2025-12-31'),
    ('Curitiba',       'PR', 'MORTALIDADE_INFANTIL',          7, DATE '2025-12-31'),
    ('Curitiba',       'PR', 'TRANSPARENCIA_PUBLICA',        94, DATE '2025-12-31'),
    ('Curitiba',       'PR', 'SERVICOS_DIGITAIS',            88, DATE '2025-12-31'),
    ('Curitiba',       'PR', 'PARTICIPACAO_CIDADA',           7, DATE '2025-12-31'),
    ('Florianópolis',  'SC', 'EMISSOES_CO2_PER_CAPITA',     1.6, DATE '2025-12-31'),
    ('Florianópolis',  'SC', 'ENERGIA_RENOVAVEL',            85, DATE '2025-12-31'),
    ('Florianópolis',  'SC', 'RECICLAGEM_RESIDUOS',          21, DATE '2025-12-31'),
    ('Florianópolis',  'SC', 'COBERTURA_SANEAMENTO',         74, DATE '2025-12-31'),
    ('Florianópolis',  'SC', 'ACESSO_TRANSPORTE_PUBLICO',    52, DATE '2025-12-31'),
    ('Florianópolis',  'SC', 'MORTALIDADE_INFANTIL',        7.2, DATE '2025-12-31'),
    ('Florianópolis',  'SC', 'TRANSPARENCIA_PUBLICA',        85, DATE '2025-12-31'),
    ('Florianópolis',  'SC', 'SERVICOS_DIGITAIS',            78, DATE '2025-12-31'),
    ('Florianópolis',  'SC', 'PARTICIPACAO_CIDADA',         5.2, DATE '2025-12-31'),
    ('Belo Horizonte', 'MG', 'EMISSOES_CO2_PER_CAPITA',     2.2, DATE '2025-12-31'),
    ('Belo Horizonte', 'MG', 'ENERGIA_RENOVAVEL',            80, DATE '2025-12-31'),
    ('Belo Horizonte', 'MG', 'RECICLAGEM_RESIDUOS',           9, DATE '2025-12-31'),
    ('Belo Horizonte', 'MG', 'COBERTURA_SANEAMENTO',         92, DATE '2025-12-31'),
    ('Belo Horizonte', 'MG', 'ACESSO_TRANSPORTE_PUBLICO',    65, DATE '2025-12-31'),
    ('Belo Horizonte', 'MG', 'MORTALIDADE_INFANTIL',        9.8, DATE '2025-12-31'),
    ('Belo Horizonte', 'MG', 'TRANSPARENCIA_PUBLICA',        88, DATE '2025-12-31'),
    ('Belo Horizonte', 'MG', 'SERVICOS_DIGITAIS',            80, DATE '2025-12-31'),
    ('Belo Horizonte', 'MG', 'PARTICIPACAO_CIDADA',         3.8, DATE '2025-12-31'),
    ('São Paulo',      'SP', 'EMISSOES_CO2_PER_CAPITA',     2.4, DATE '2025-12-31'),
    ('São Paulo',      'SP', 'ENERGIA_RENOVAVEL',            72, DATE '2025-12-31'),
    ('São Paulo',      'SP', 'RECICLAGEM_RESIDUOS',           7, DATE '2025-12-31'),
    ('São Paulo',      'SP', 'COBERTURA_SANEAMENTO',         96, DATE '2025-12-31'),
    ('São Paulo',      'SP', 'ACESSO_TRANSPORTE_PUBLICO',    70, DATE '2025-12-31'),
    ('São Paulo',      'SP', 'MORTALIDADE_INFANTIL',       10.5, DATE '2025-12-31'),
    ('São Paulo',      'SP', 'TRANSPARENCIA_PUBLICA',        90, DATE '2025-12-31'),
    ('São Paulo',      'SP', 'SERVICOS_DIGITAIS',            88, DATE '2025-12-31'),
    ('São Paulo',      'SP', 'PARTICIPACAO_CIDADA',         2.1, DATE '2025-12-31'),
    ('Recife',         'PE', 'EMISSOES_CO2_PER_CAPITA',       2, DATE '2025-12-31'),
    ('Recife',         'PE', 'ENERGIA_RENOVAVEL',            76, DATE '2025-12-31'),
    ('Recife',         'PE', 'RECICLAGEM_RESIDUOS',           5, DATE '2025-12-31'),
    ('Recife',         'PE', 'COBERTURA_SANEAMENTO',         44, DATE '2025-12-31'),
    ('Recife',         'PE', 'ACESSO_TRANSPORTE_PUBLICO',    58, DATE '2025-12-31'),
    ('Recife',         'PE', 'MORTALIDADE_INFANTIL',       12.5, DATE '2025-12-31'),
    ('Recife',         'PE', 'TRANSPARENCIA_PUBLICA',        82, DATE '2025-12-31'),
    ('Recife',         'PE', 'SERVICOS_DIGITAIS',            74, DATE '2025-12-31'),
    ('Recife',         'PE', 'PARTICIPACAO_CIDADA',           3, DATE '2025-12-31'),
    ('Belém',          'PA', 'EMISSOES_CO2_PER_CAPITA',     1.8, DATE '2025-12-31'),
    ('Belém',          'PA', 'ENERGIA_RENOVAVEL',            90, DATE '2025-12-31'),
    ('Belém',          'PA', 'RECICLAGEM_RESIDUOS',           3, DATE '2025-12-31'),
    ('Belém',          'PA', 'COBERTURA_SANEAMENTO',         18, DATE '2025-12-31'),
    ('Belém',          'PA', 'ACESSO_TRANSPORTE_PUBLICO',    45, DATE '2025-12-31'),
    ('Belém',          'PA', 'MORTALIDADE_INFANTIL',         15, DATE '2025-12-31'),
    ('Belém',          'PA', 'TRANSPARENCIA_PUBLICA',        70, DATE '2025-12-31'),
    ('Belém',          'PA', 'SERVICOS_DIGITAIS',            55, DATE '2025-12-31'),
    ('Belém',          'PA', 'PARTICIPACAO_CIDADA',         2.5, DATE '2025-12-31'),
    ('Curitiba',       'PR', 'ENERGIA_RENOVAVEL',            88, DATE '2024-12-31'),
    ('Recife',         'PE', 'COBERTURA_SANEAMENTO',         41, DATE '2024-12-31'),
    ('São Paulo',      'SP', 'RECICLAGEM_RESIDUOS',           6, DATE '2024-12-31'),
    ('Belém',          'PA', 'SERVICOS_DIGITAIS',            48, DATE '2024-12-31')
) AS v (nome, uf, tipo, valor, data_referencia)
JOIN cidade c ON c.nome = v.nome AND c.uf = v.uf;
