package com.ipiecoles.java.java230;

import com.ipiecoles.java.java230.exceptions.BatchException;
import com.ipiecoles.java.java230.exceptions.TechnicienException;
import com.ipiecoles.java.java230.model.Commercial;
import com.ipiecoles.java.java230.model.Employe;
import com.ipiecoles.java.java230.model.Manager;
import com.ipiecoles.java.java230.model.Technicien;
import com.ipiecoles.java.java230.repository.BaseEmployeRepository;
import com.ipiecoles.java.java230.repository.EmployeRepository;
import com.ipiecoles.java.java230.repository.ManagerRepository;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MyRunner implements CommandLineRunner {

	private static final String REGEX_MATRICULE = "^[MTC][0-9]{5}$";
	private static final String REGEX_NOM = ".*";
	private static final String REGEX_PRENOM = ".*";
	private static final int NB_CHAMPS_MANAGER = 5;
	private static final int NB_CHAMPS_TECHNICIEN = 7;
	private static final String REGEX_MATRICULE_MANAGER = "^M[0-9]{5}$";
	private static final int NB_CHAMPS_COMMERCIAL = 7;
	private static final String REGEX_FIRST_LETTER = "^[MTC]{1}.*";
	private static final String REGEX_MATRICULE_COMMERCIAL = "^C[0-9]{5}$";
	private static final String REGEX_MATRICULE_TECHNICIEN = "^T[0-9]{5}$";
	@Autowired
	private EmployeRepository employeRepository;

	@Autowired
	private ManagerRepository managerRepository;

	private List<Employe> employes = new ArrayList<Employe>();

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void run(String... strings) {
		String fileName = "employes.csv";
		try {
			readFile(fileName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// readFile(strings[0]);
	}

	/**
	 * Méthode qui lit le fichier CSV en paramètre afin d'intégrer son contenu en
	 * BDD
	 * 
	 * @param fileName Le nom du fichier (à mettre dans src/main/resources)
	 * @return une liste contenant les employés à insérer en BDD ou null si le
	 *         fichier n'a pas pu être le
	 */
	public List<Employe> readFile(String fileName) {
		Stream<String> stream = null;
		try {
			stream = Files.lines(Paths.get(new ClassPathResource(fileName).getURI()));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// TODO

		Integer i = 0;
		for (String ligne : stream.collect(Collectors.toList())) {
			i++;
			try {
				processLine(ligne);
			} catch (BatchException e) {
				System.out.println("Ligne " + i + " : " + e.getMessage() + " => " + ligne);

			}
		}

		return employes;
	}

	/**
	 * Méthode qui regarde le premier caractère de la ligne et appelle la bonne
	 * méthode de création d'employé
	 * 
	 * @param ligne la ligne à analyser
	 * @throws BatchException si le type d'employé n'a pas été reconnu
	 */
	private void processLine(String ligne) throws BatchException {

		String[] tab = ligne.split(",");

		if (!ligne.matches(REGEX_FIRST_LETTER)) {

			throw new BatchException("Type d'employé inconnu : " + ligne.charAt(0));
		}

		processManager(ligne);
		processCommercial(ligne);
		processTechnicien(ligne);

	}

	/**
	 * Méthode qui crée un Commercial à partir d'une ligne contenant les
	 * informations d'un commercial et l'ajoute dans la liste globale des employés
	 * 
	 * @param ligneCommercial la ligne contenant les infos du commercial à intégrer
	 * @throws BatchException s'il y a un problème sur cette ligne
	 */
	private void processCommercial(String ligneCommercial) throws BatchException {
		// TODO

		String[] tabCommercial = ligneCommercial.split(",");

		if (tabCommercial[0].matches("^[C]{1}.*") && !tabCommercial[0].matches(REGEX_MATRICULE_COMMERCIAL)) {

			throw new BatchException(
					"La chaîne " + tabCommercial[0] + " ne respecte pas l'expression régulière ^[MTC][0-9]{5}$C12");
		}

		if (tabCommercial[0].matches(REGEX_MATRICULE_COMMERCIAL) && tabCommercial.length != NB_CHAMPS_COMMERCIAL) {
			
			throw new BatchException("La ligne commercial ne contient pas 7 éléments mais " + tabCommercial.length);
		}

		if (tabCommercial[0].matches(REGEX_MATRICULE_COMMERCIAL) && tabCommercial.length == NB_CHAMPS_COMMERCIAL) {

			String CaAnnuel = tabCommercial[5];
			try {

				Double.parseDouble(CaAnnuel);

			} catch (Exception e) {

				throw new BatchException(
						tabCommercial[5] + "n'est pas un nombre valide pour Chiffre d'affaire annuel ");
			}
			
			
			try {
				Integer.parseInt(tabCommercial[6]);
			}catch(Exception e ) {
				
				throw new BatchException("La performance du commercial est incorrecte : " + tabCommercial[6]);
			}

			String salaireCommercial = tabCommercial[4];

			try {
				Double.parseDouble(salaireCommercial);

			} catch (Exception e) {

				throw new BatchException(tabCommercial[4] + "n'est pas un nombre valide pour un salaire ");
			}

			String dateEmbaucheCommercial = tabCommercial[3];

			try {

				DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(dateEmbaucheCommercial);

			} catch (Exception e) {

				throw new BatchException(tabCommercial[3] + " ne respecte pas le format de date dd/MM/yyyy");
			}

			String nom = tabCommercial[1];
			String prenom = tabCommercial[2];
			String matriculeCommercial = tabCommercial[0];
			LocalDate date = DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(dateEmbaucheCommercial);
			Double salaire = Double.parseDouble(salaireCommercial);
			Double CaAnnuelCommercial = Double.parseDouble(CaAnnuel);
			Commercial c = new Commercial(nom, prenom, matriculeCommercial, date, salaire, CaAnnuelCommercial);
			employes.add(c);
			System.out.println(c);
		}

	}

	/**
	 * Méthode qui crée un Manager à partir d'une ligne contenant les informations
	 * d'un manager et l'ajoute dans la liste globale des employés
	 * 
	 * @param ligneManager la ligne contenant les infos du manager à intégrer
	 * @throws BatchException s'il y a un problème sur cette ligne
	 */
	private void processManager(String ligneManager) throws BatchException {
		// TODO
		String[] tabManager = ligneManager.split(",");

		if (tabManager[0].matches("^[M]{1}.*") && !tabManager[0].matches(REGEX_MATRICULE_MANAGER)) {

			throw new BatchException(
					"la chaîne " + tabManager[0] + " ne respecte pas l'expression régulière ^[MTC][0-9]{5}$M12");
		}

		if (tabManager[0].matches(REGEX_MATRICULE_MANAGER) && tabManager.length != NB_CHAMPS_MANAGER) {
			throw new BatchException("La ligne manager ne contient pas 5 éléments mais " + tabManager.length);
		}

		if (tabManager[0].matches(REGEX_MATRICULE_MANAGER) && tabManager.length == NB_CHAMPS_MANAGER) {
			String dateEmbaucheManager = tabManager[3];

			try {
				if (tabManager[0].matches("^[M]{1}.*")) {

					DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(dateEmbaucheManager);
				}

			} catch (Exception e) {

				throw new BatchException(tabManager[3] + " ne respecte pas le format de date dd/MM/yyyy");
			}

			String salaireManager = tabManager[4];

			if (tabManager[0].matches("^[M]{1}.*")) {
				try {

					Double.parseDouble(salaireManager);

				} catch (Exception e) {

					throw new BatchException(tabManager[4] + " n'est pas un nombre valide pour un salaire ");
				}
			}

			String nom = tabManager[1];
			String prenom = tabManager[2];
			String matriculeManager = tabManager[0];
			LocalDate date = DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(dateEmbaucheManager);
			Double salaire = Double.parseDouble(salaireManager);
			Manager m = new Manager(nom, prenom,matriculeManager,date, salaire, null);
			employes.add(m);
			System.out.println(m);
		}
	}

	/**
	 * Méthode qui crée un Technicien à partir d'une ligne contenant les
	 * informations d'un technicien et l'ajoute dans la liste globale des employés
	 * 
	 * @param ligneTechnicien la ligne contenant les infos du technicien à intégrer
	 * @throws BatchException      s'il y a un problème sur cette ligne
	 * @throws TechnicienException
	 */
	private void processTechnicien(String ligneTechnicien) throws BatchException {
		// TODO

		String[] tabTechnicien = ligneTechnicien.split(",");

		if (tabTechnicien[0].matches("^[T]{1}.*") && !tabTechnicien[0].matches(REGEX_MATRICULE_TECHNICIEN)) {

			throw new BatchException(
					"la chaîne " + tabTechnicien[0] + " ne respecte pas l'expression régulière ^[T][0-9]{5}$T12");
		}

		if (tabTechnicien[0].matches(REGEX_MATRICULE_TECHNICIEN) && tabTechnicien.length != NB_CHAMPS_TECHNICIEN) {
			throw new BatchException("La ligne technicien ne contient pas 7 éléments mais " + tabTechnicien.length);
		}

		if (tabTechnicien[0].matches(REGEX_MATRICULE_TECHNICIEN) && tabTechnicien.length == NB_CHAMPS_TECHNICIEN) {

			try {

				Integer.parseInt(tabTechnicien[5]);

			} catch (Exception e) {

				throw new BatchException("Le grade du technicien est incorrect: " + tabTechnicien[5]);

			}

			Integer grade = Integer.parseInt(tabTechnicien[5]);

			if (grade <= 0 || grade > 5) {
				throw new BatchException("Le grade doit être compris entre 1 et 5 : " + grade);
			}

			if (!tabTechnicien[6].matches(REGEX_MATRICULE_MANAGER)) {

				throw new BatchException(

						" la chaîne " + tabTechnicien[6] + " ne respecte pas l'expression régulière ^M[0-9]{5}$");

			}

			String salaireTechnicien = tabTechnicien[4];

			try {

				Double.parseDouble(salaireTechnicien);

			} catch (Exception e) {

				throw new BatchException(tabTechnicien[4] + " n'est pas un nombre valide pour un salaire ");

			}

			String dateEmbaucheTechnicien = tabTechnicien[3];

			try {

				DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(dateEmbaucheTechnicien);

			} catch (Exception e) {

				throw new BatchException(tabTechnicien[3] + " ne respecte pas le format de date dd/MM/yyyy");
			}

			if (tabTechnicien[6].matches(REGEX_MATRICULE_MANAGER)) {

				Manager TechnicienManager = managerRepository.findByMatricule(tabTechnicien[6]);

				if (TechnicienManager == null) {

					throw new BatchException(" le matricule " + tabTechnicien[6] + " n'existe pas en base de données");
				}
			}

			String nom = tabTechnicien[1];
			String prenom = tabTechnicien[2];
			String matricule = tabTechnicien[0];
			LocalDate date = DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(tabTechnicien[3]);
			Double salaire = Double.parseDouble(tabTechnicien[4]);

			Technicien t = new Technicien(nom, prenom, matricule, date, salaire, grade);
			System.out.println(t);
			employes.add(t);

		}

	}
}
