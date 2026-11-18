---
name: feature-com-spec
description: Conduz o desenvolvimento de qualquer feature seguindo o fluxo SDD (Spec-Driven Development) — contexto (AGENTS.md) → especificação (docs/spec.md) → plano (docs/plano.md) → implementação incremental → validação.
---

# Skill: Feature com Spec (SDD)

## Objetivo

Implementar uma nova feature em qualquer projeto seguindo rigorosamente o fluxo **spec → plano → implementação → validação**, garantindo alinhamento de requisitos antes de escrever código, rastreabilidade por tarefas pequenas e preservação das funcionalidades existentes.

---

## Entradas

- **Descrição da feature:** problema a resolver, comportamento esperado, restrições e critérios desejados.
- **Projeto / Repositório:** caminho ou workspace do projeto onde a feature será implementada.

---

## Regras Inegociáveis

1. **Não escrever código antes da spec aprovada:** nenhum arquivo de produção ou teste deve ser criado/alterado antes da aprovação explícita de `docs/spec.md`.
2. **Uma tarefa por vez:** implementar apenas uma tarefa do `docs/plano.md` por ciclo, aguardando validação antes de avançar para a próxima.
3. **Um commit por tarefa:** cada tarefa concluída e validada deve gerar seu próprio commit com mensagem clara.
4. **Não alterar o que estiver fora do escopo:** preservar integralmente funcionalidades, contratos e convenções existentes que não façam parte da feature.
5. **Spec sempre à frente do código:** se durante a implementação for descoberta uma lacuna ou erro de requisito, atualizar `docs/spec.md` primeiro e só depois ajustar o código.

---

## Passo a Passo (Etapas 2 a 6)

### Etapa 2: Contexto para o agente (`AGENTS.md`)

**O que gerar:**
Explore o repositório e gere (ou atualize, se já existir) o arquivo `AGENTS.md` na raiz do projeto contendo:
- **Visão geral:** o que a aplicação faz e quais funcionalidades já existem.
- **Stack e estrutura:** linguagens, frameworks, ferramentas e organização das pastas.
- **Como executar e testar:** comandos exatos para compilar, rodar a aplicação e executar os testes.
- **Convenções:** padrões de nomes, organização de classes/módulos e estilo de código.
- **Restrições:** o que o agente não deve fazer sem pedir (ex.: alterar funcionalidades existentes fora do escopo da feature, quebrar formatos de persistência ou contratos públicos).

**O que revisar:**
- Validar se todos os caminhos de arquivos, comandos de execução/teste e convenções refletem fielmente o repositório real.
- Corrigir qualquer detalhe incorreto, desatualizado ou incompleto antes de avançar.

---

### Etapa 3: Especificação (`docs/spec.md`)

**O que gerar:**
1. **Antes de escrever o documento:** analise a descrição da feature frente ao `AGENTS.md` e ao código existente e **faça perguntas objetivas** ao usuário para esclarecer ambiguidades, casos de borda e decisões de design.
2. **Após as respostas:** crie `docs/spec.md` usando o modelo da seção **Modelos**, contendo:
   - **Contexto e objetivo:** qual problema a feature resolve.
   - **Regras de negócio:** comportamento esperado, validações e casos de borda.
   - **Critérios de aceite:** cenários testáveis no formato **Dado / Quando / Então**.
   - **Impacto no que já existe:** quais módulos, fluxos ou funcionalidades atuais podem ser afetados.
   - **Fora do escopo:** o que esta feature explicitamente não fará.

**O que revisar:**
- Verificar se não há contradições com o sistema atual nem requisitos vagos.
- Ajustar a spec até aprovação explícita e **fazer o commit da spec aprovada** antes de iniciar o plano/código.

---

### Etapa 4: Plano de implementação (`docs/plano.md`)

**O que gerar:**
Leia `docs/spec.md` e `AGENTS.md` e proponha um plano dividido em **tarefas pequenas e numeradas** (com checkboxes `- [ ]` em `docs/plano.md`), indicando para cada tarefa:
- **O que será feito.**
- **Quais arquivos serão criados ou alterados.**
- **Como validar** que a tarefa está pronta (comando de teste automatizado e/ou conferência manual).

**O que revisar:**
- Avaliar a ordem lógica das tarefas (dependências de backend/modelo antes de UI/integração, testes automatizados contemplados).
- Verificar o tamanho de cada tarefa; se alguma estiver grande demais ou misturar responsabilidades, dividi-la antes de aprovar e gerar `docs/plano.md`.

---

### Etapa 5: Implementação incremental

**O que gerar:**
Implemente **uma tarefa por vez**, sempre referenciando `docs/spec.md` e `docs/plano.md`. A cada tarefa:
1. Aplicar somente as alterações previstas na tarefa atual.
2. Apresentar o resumo das mudanças / diff gerado e sugerir uma mensagem de commit clara.
3. Executar a compilação e os testes aplicáveis.
4. Aguardar a revisão do usuário antes de marcar `- [x]` no `docs/plano.md` e seguir para a próxima tarefa.

**O que revisar (a cada tarefa):**
- Ler o diff gerado e entender exatamente o que mudou.
- Rodar a aplicação e os testes.
- Solicitar correções se necessário (ou atualizar `docs/spec.md` primeiro caso o requisito tenha mudado).
- Marcar a tarefa como concluída (`- [x]`) em `docs/plano.md` e fazer **um commit por tarefa** com mensagem clara.

---

### Etapa 6: Validação

**O que gerar / executar:**
1. **Testes automatizados:** garantir que existam testes automatizados cobrindo a feature usando o stack do projeto (ex.: **Spock** em projetos Java/Groovy, ou o framework de testes configurado no projeto para JS/outras linguagens).
2. **Auditoria de critérios de aceite:** conferir, **um a um**, se todos os cenários **Dado / Quando / Então** de `docs/spec.md` foram atendidos, distinguindo o que foi validado por teste automatizado do que exige verificação manual.
3. **Regressão manual:** testar manualmente as funcionalidades que já existiam no projeto para garantir que nada quebrou fora do escopo.

**O que revisar:**
- Confirmar que todos os testes automatizados passam sem falhas nem assertions relaxadas.
- Confirmar que nenhum requisito fora do escopo foi alterado indevidamente e que todas as tarefas em `docs/plano.md` estão marcadas como concluídas.

---

## Modelos

### Modelo esperado de `docs/spec.md`

```markdown
# Especificação Técnica: <Nome da Feature>

## 1. Contexto e Objetivo
Descreva o problema atual e o objetivo da feature.

## 2. Regras de Negócio
1. Regra principal...
2. Casos de borda:
   - Cenário de erro / indisponibilidade / entrada inválida...

## 3. Critérios de Aceite
- **Cenário 1: <Título>**
  - **Dado** <estado inicial>
  - **Quando** <ação executada>
  - **Então** <resultado esperado>

## 4. Impacto no que já existe
- `<arquivo ou módulo>`: o que muda e cuidados para não quebrar fluxos atuais.

## 5. Fora do Escopo
- Itens que não serão implementados nesta feature.
```

### Modelo esperado de `docs/plano.md`

```markdown
# Plano de Implementação

Este plano divide a implementação de [docs/spec.md](spec.md) em tarefas pequenas e numeradas, respeitando [AGENTS.md](../AGENTS.md).

- [ ] **1. <Título curto da tarefa>**
  - **O que será feito:** <descrição objetiva>
  - **Arquivos criados ou alterados:**
    - Criar/Alterar: `<caminho/do/arquivo>`
  - **Como validar:**
    - <comando de teste automatizado e/ou passos de conferência manual>

- [ ] **2. <Título curto da tarefa>**
  - **O que será feito:** <descrição objetiva>
  - **Arquivos criados ou alterados:**
    - Criar/Alterar: `<caminho/do/arquivo>`
  - **Como validar:**
    - <comando de teste automatizado e/ou passos de conferência manual>
```
