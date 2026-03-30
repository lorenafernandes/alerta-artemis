# 🏹 Alerta Ártemis
**Sistema Mobile de Segurança Pessoal e Acionamento Rápido de Rede de Apoio.**

> **Status do Projeto:** MVP Concluído 🚀

## 📋 Sobre o Projeto
O **Alerta Ártemis** é um aplicativo Android nativo desenvolvido para garantir a segurança pessoal através do acionamento rápido e silencioso de uma rede de apoio. Em situações de emergência ou vulnerabilidade, o tempo e a discrição são cruciais.

O sistema permite que a usuária cadastre contatos de confiança e, através de uma interface de acionamento único (Single-Action UI), dispare mensagens de socorro via SMS contendo sua localização exata em tempo real, sem a necessidade de conexão com a internet (pacote de dados).

## 🛠 Tecnologias e Arquitetura
* **Plataforma:** Android Nativo (API 24+)
* **Linguagem:** Java
* **Interface (UI/UX):** XML, ConstraintLayout, Material Design Components.
* **Armazenamento Local:** `SharedPreferences` (Persistência leve e offline de contatos).
* **Hardware Interfacing:** * `LocationManager` (Coordenadas GPS e Network).
    * `SmsManager` (Despacho de pacotes SMS em background).

## ⚙️ Funcionalidades Principais
* **Single-Button SOS:** Botão central de emergência com acionamento focado.
* **Anti-Toque Acidental:** Implementação de `Handler` e `Runnable` para exigir o pressionamento contínuo por 3 segundos, cancelando a ação se o toque for interrompido.
* **Captura de Telemetria (GPS):** Leitura de coordenadas latitude/longitude via satélite ou triangulação de antenas.
* **Comunicação Resiliente:** Envio de SMS invisível e em lote para até 3 contatos de segurança, garantindo a entrega do pedido de socorro com um link rastreável do Google Maps, mesmo sem acesso ao Wi-Fi ou 3G/4G.
* **Gestão de Contatos Dinâmica:** Interface baseada no padrão *Accordion* para gerenciar a Rede de Apoio.

## 🧠 Decisões de Engenharia (Para Avaliação)
Durante o desenvolvimento do MVP, escolhas arquiteturais específicas foram tomadas visando a **resiliência** do software:

1.  **Por que SMS e não WhatsApp API/Firebase?**
    Em uma emergência, a usuária pode estar em áreas de sombra de cobertura de dados de internet (3G/4G/5G). O protocolo SMS utiliza a rede de voz (GSM), que possui a maior capilaridade e estabilidade de sinal em território nacional. O app funciona mesmo se a usuária estiver sem pacote de dados.
2.  **Por que `SharedPreferences` em vez de SQLite/Room?**
    A regra de negócio restringe a rede de apoio a no máximo 3 contatos. Para uma estrutura de dados tão unifilar, a sobrecarga de instanciar um banco de dados relacional (SQLite) seria desnecessária. O uso de chave-valor garante tempo de leitura O(1) na hora do disparo da emergência.
3.  **Degradação Graciosa (Graceful Degradation):**
    Se o hardware de GPS falhar ou estiver desativado, o aplicativo não sofre *crash*. Ele captura o erro, adapta a string de alerta informando a indisponibilidade do sinal, e realiza o envio do pedido de socorro mesmo sem as coordenadas.

## 📱 Como Executar o Projeto (Guia do Avaliador)
**Atenção Avaliador:** Por se tratar de um aplicativo que consome serviços reais de telecomunicação, **NÃO** é recomendado o teste em emuladores (AVD), pois eles não despacham SMS para números reais e não possuem antenas de GPS.

1.  Clone este repositório: `git clone https://github.com/SEU_USUARIO/alerta-artemis.git`
2.  Abra o projeto no Android Studio.
3.  Compile e instale o APK em um **dispositivo físico** equipado com um SIM Card válido e com saldo/plano ativo para envio de SMS.
4.  Ao abrir o aplicativo pela primeira vez, conceda as permissões de Localização e Envio de SMS.
5.  Navegue até a aba "Rede de Apoio" (ícone de telefone) e cadastre um número de teste real (utilize o formato numérico com DDD).
6.  Retorne à tela principal e mantenha o botão central pressionado por 3 segundos.
7.  Verifique o recebimento da mensagem no aparelho de destino.

## 👩‍💻 Autoria
Desenvolvido por **GRUPO 24** da disciplina de **DESENVOLVIMENTO PARA PLATAFORMAS MÓVEIS**, composto por:
* Ives Carneiro Sebestyen - Matric. 2412834
* Joedson Bezerra de Souza Filho - Matric. 2418744
* Liedson Kauam Oliveira Fernandes - Matric. 2425131
* Lorena Kailany Oliveira Fernandes - Matric. 2425133
