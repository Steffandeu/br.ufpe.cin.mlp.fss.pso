import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import util.Util;

public class Main {

	// Parametros
	private static int iteracoes = 20;

	private static double taxaAprendizagem;

	private static int numeroNeuroniosCamadaEntrada = 4;

	private static int numeroNeuroniosCamadaSaida = 3;

	private static int numeroNeuroniosCamadaEscondida = 6;

	public static void main(String[] args) {

		ArrayList<String[]> datasetCarregado = new ArrayList<String[]>();

		// Realiza a leitura do csv para um array
		try {

			datasetCarregado = Util
					.leituraCSV(
							"/home/ejmvs/workspace/br.ufpe.cin.mlp.fss.pso/src/dataset/iris_mapped.csv",
							",");

		} catch (IOException e1) {
			System.out.println("Erro no carregamento do arquivo: "
					+ e1.toString());
		}

		System.out
				.println("-----------------REDE NEURAL - MLP-----------------");
		System.out.println("TAXA DE APRENDIZAGEM = " + taxaAprendizagem);
		System.out.println("QUANTIDADE DE ITERACOES = " + iteracoes + "\n");

		MLPHibrida mlp = new MLPHibrida(numeroNeuroniosCamadaEntrada,
				numeroNeuroniosCamadaEscondida, numeroNeuroniosCamadaSaida);

		// Inicio do treinamento
		System.out.println("Training...");

		// Embaralhando o dataset
		Collections.shuffle(datasetCarregado);

		// Quantidade de linhas para treinar com 90% dos dados
		int qtdLinhasNoventaPorcento = (int) Math
				.round(datasetCarregado.size() * 0.09);

		ArrayList<String[]> datasetTreino = new ArrayList<String[]>();
		ArrayList<String[]> datasetTeste = new ArrayList<String[]>();

		// Inicializa o dataset de treino
		for (int i = 0; i < qtdLinhasNoventaPorcento; i++) {
			datasetTreino.add(datasetCarregado.get(i));
		}

		// Inicializa o dataset de teste
		for (int i = qtdLinhasNoventaPorcento; i < datasetCarregado.size(); i++) {
			datasetTeste.add(datasetCarregado.get(i));
		}

		for (int i = 0; i < iteracoes; i++) {

			for (Iterator iterator = datasetTreino.iterator(); iterator
					.hasNext();) {
				String[] linha = (String[]) iterator.next();

				// TODO: Tentar transformar de forma generica

				// Converte a linha do dataset para treinar a rede MLP
				double[] padrao = new double[4];
				padrao[0] = Double.parseDouble(linha[0]);
				padrao[1] = Double.parseDouble(linha[1]);
				padrao[2] = Double.parseDouble(linha[2]);
				padrao[3] = Double.parseDouble(linha[3]);

				// Converte a saida esperada para o treinamento
				double[] saidaEsperada = new double[3];
				saidaEsperada[0] = Double.parseDouble(linha[4]);
				saidaEsperada[1] = Double.parseDouble(linha[5]);
				saidaEsperada[2] = Double.parseDouble(linha[6]);

				// Treinamento para a rede neural
				mlp.treinamento(padrao, saidaEsperada,
						mlp.TREINAMENTO_BACK_PROPAGATION);

			}
		}

		String[] linhaTempTeste = (String[]) datasetTreino.get(0);

		// TODO: Tentar transformar de forma generica

		// Converte a linha do dataset para treinar a rede MLP
		double[] padrao = new double[4];
		padrao[0] = Double.parseDouble(linhaTempTeste[0]);
		padrao[1] = Double.parseDouble(linhaTempTeste[1]);
		padrao[2] = Double.parseDouble(linhaTempTeste[2]);
		padrao[3] = Double.parseDouble(linhaTempTeste[3]);

		// Converte a saida esperada para o treinamento
		double[] saidaEsperada = new double[3];
		saidaEsperada[0] = Double.parseDouble(linhaTempTeste[4]);
		saidaEsperada[1] = Double.parseDouble(linhaTempTeste[5]);
		saidaEsperada[2] = Double.parseDouble(linhaTempTeste[6]);

		double[] saidaRede = mlp.apresentaPadrao(padrao);

		System.out.println("SAIDA ESPERADA: " + saidaEsperada[0] + " - "
				+ saidaEsperada[1] + " - " + saidaEsperada[2]);

		System.out.println("SAIDA DA MLP: " + saidaRede[1] + " - "
				+ saidaRede[2] + " - " + saidaRede[3]);

	}

}