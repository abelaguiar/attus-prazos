CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE IF EXISTS prazo
    ADD COLUMN IF NOT EXISTS descricao_hash varchar(64);

ALTER TABLE IF EXISTS prazo
    ALTER COLUMN descricao TYPE text;

ALTER TABLE IF EXISTS prazo
    DROP CONSTRAINT IF EXISTS uk_prazo_processo_descricao_data;

UPDATE prazo
SET numero_processo = regexp_replace(numero_processo, '\D', '', 'g'),
    descricao_hash = encode(digest(descricao, 'sha256'), 'hex')
WHERE numero_processo <> regexp_replace(numero_processo, '\D', '', 'g')
   OR descricao_hash IS NULL
   OR descricao_hash <> encode(digest(descricao, 'sha256'), 'hex');

ALTER TABLE IF EXISTS prazo
    ALTER COLUMN descricao_hash SET NOT NULL;

-- Indice unico idempotente. Evitamos o bloco DO $$...$$ porque o ScriptUtils do Spring
-- quebra o script nos ';' internos do bloco. CREATE UNIQUE INDEX IF NOT EXISTS e' nativo,
-- idempotente, e reaproveita o indice ja criado pelo Hibernate caso exista (mesmo nome).
CREATE UNIQUE INDEX IF NOT EXISTS uk_prazo_processo_descricao_hash_data
    ON prazo (numero_processo, descricao_hash, data_prazo);
