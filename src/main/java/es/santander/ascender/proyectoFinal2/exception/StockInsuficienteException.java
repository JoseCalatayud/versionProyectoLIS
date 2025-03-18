package es.santander.ascender.proyectoFinal2.exception;

public class StockInsuficienteException extends RuntimeException {
    
    private String nombreArticulo;
    private Integer stockDisponible;
    private Integer cantidadSolicitada;
    
    public StockInsuficienteException(String nombreArticulo, Integer stockDisponible, Integer cantidadSolicitada) {
        super("Stock insuficiente para el artículo: " + nombreArticulo + 
              ". Stock disponible: " + stockDisponible + ", Cantidad solicitada: " + cantidadSolicitada);
        this.nombreArticulo = nombreArticulo;
        this.stockDisponible = stockDisponible;
        this.cantidadSolicitada = cantidadSolicitada;
    }
    
    public String getNombreArticulo() {
        return nombreArticulo;
    }
    
    public Integer getStockDisponible() {
        return stockDisponible;
    }
    
    public Integer getCantidadSolicitada() {
        return cantidadSolicitada;
    }
}