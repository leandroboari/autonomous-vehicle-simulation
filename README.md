# Simulação de Veículos Autônomos

Disciplina: Automação Avançada\n
Curso: Engenharia de Controle e Automação\n
Autor: Leandro Boari Naves Silva\n
Professor: Arthur de Miranda Neto\n

## Visão Geral
Este repositório contém uma simulação de veículos autônomos desenvolvida para um projeto da disciplina de Automação Avançada. A simulação inclui múltiplos carros navegando por uma pista com sensores integrados para detectar obstáculos, evitar colisões e contar voltas.

## Funcionalidades
- Simulação com Vários Carros: Simula múltiplos carros em uma pista, com cada veículo movendo-se pixel a pixel e rastreando a distância percorrida.
- Contagem de Voltas: O sistema rastreia o número de voltas completadas por cada carro ao cruzar as linhas de início e fim definidas.
- Detecção de Colisões: Os carros detectam e evitam obstáculos e outros veículos usando sensores para evitar colisões.
- Consumo de Combustível: Cada carro possui um tanque de combustível, e o consumo é monitorado conforme a simulação avança.
- Funcionalidade de Pausa/Retomada: Controle a simulação através dos botões de início, pausa e finalização.
- Threads e Sincronização: Os carros rodam em threads separadas, aproveitando o multithreading para garantir desempenho fluido.
- Polimorfismo e Semáforos: Gerencie o movimento simultâneo dos carros, controlando zonas restritas onde apenas um carro pode passar por vez.
- Integração com Firebase: Armazene o estado atual da simulação no Firestore, permitindo a continuidade da corrida em outro momento.

## Como Funciona
- Configuração da Pista: A pista é uma imagem branca com bordas e obstáculos pretos. Os carros começam na linha de largada definida e se movem pixel a pixel.
- Sensores: Cada carro é equipado com um sensor de distância para detectar obstáculos e outros carros, evitando colisões.
- Movimentação dos Carros: A lógica de movimentação é gerenciada por threads separadas, com cada carro verificando seu ambiente antes de se mover.
- Integração com Firebase: Ao pausar ou finalizar a corrida, o estado do carro (posição, nível de combustível, etc.) é salvo no Firestore.
