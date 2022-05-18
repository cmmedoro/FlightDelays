package it.polito.tdp.extflightdelays.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {
	
	private Graph<Airport, DefaultWeightedEdge> grafo;
	private ExtFlightDelaysDAO dao;
	private Map<Integer, Airport> idMap;
	
	public Model() {
		this.dao = new ExtFlightDelaysDAO();
		this.idMap = new HashMap<Integer, Airport>(); 
		dao.loadAllAirports(idMap);
	}
	
	public void creaGrafo(int x) {
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		//aggiungere i vertici ---> sottoinsieme
		Graphs.addAllVertices(this.grafo, this.dao.getVertici(x, idMap));
		//aggiungere gli archi
		for(Rotta r : dao.getRotte(idMap)) {
			if(this.grafo.containsVertex(r.getA1()) && this.grafo.containsVertex(r.getA2())) {
				DefaultWeightedEdge edge = this.grafo.getEdge(r.getA1(), r.getA2());
				if(edge == null) { //non c'è ancora un arco fra i due aeroporti
					Graphs.addEdgeWithVertices(this.grafo, r.getA1(), r.getA2(), r.getnVoli());
				}
				else { //arco c'era già, ho già peso in un verso
					double pesoVecchio = this.grafo.getEdgeWeight(edge);
					double pesoNuovo = pesoVecchio+r.getnVoli();
					//Sovrascrivo peso nuovo a quello vecchio
					this.grafo.setEdgeWeight(edge, pesoNuovo);
				}
			}
		}
		System.out.println("# vertici: "+this.grafo.vertexSet().size()+"\n");
		System.out.println("# archi: "+this.grafo.edgeSet().size());
	}
	
	public List<Airport> getVertici(){
		//controllo che grafo sia stato creato prima di restituire i vertici
		//meglio ordinare i vertici per metterli nelle tendine
		List<Airport> vertici = new ArrayList<>(this.grafo.vertexSet());
		Collections.sort(vertici);
		return vertici;
	}

	public List<Airport> getPercorso(Airport a1, Airport a2){
		//devo controllare che i due vertici prima siano collegati (non per forza tramite collegamento diretto)
		List<Airport> percorso = new ArrayList<>();
		//devo visitare il grafo
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it = new BreadthFirstIterator<>(this.grafo, a1);
		boolean trovato = false; //se trovo il vertice a2 imposto a vero
		while(it.hasNext()) {
			Airport visitato = it.next();
			if(visitato.equals(a2)) {
				trovato = true;
			}
		}		
		//ottengo il percorso
		if(trovato) {
			//lista in cui aggiungo in testa
			percorso.add(a2);
			//dalla destinazione risalgo
			Airport step = it.getParent(a2);
			while(!step.equals(a1)) {
				percorso.add(0, step);
				step= it.getParent(step);
			}
			percorso.add(0,a1); //quando step == a1 non entro più nel while, quindi devo aggiungere il vertice fuori
			return percorso;
		}else {
			return null; //non c'è componente connessa, i due aeroporti non sono collegati ---> 
			//alternativa: connectedSetOf e vedo se ci sono nel set entrambi i vertici
		}
		
	}
}
