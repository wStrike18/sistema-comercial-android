package com.idesolution.distribuidoravdc.BD.Repository;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.LiveData;

import com.idesolution.distribuidoravdc.BD.Dao.AppDataBase;
import com.idesolution.distribuidoravdc.BD.Dao.ClienteDao;
import com.idesolution.distribuidoravdc.BD.Dao.EntregaDao;
import com.idesolution.distribuidoravdc.BD.Dao.LocalizacionDao;
import com.idesolution.distribuidoravdc.BD.Dao.SessionManager;
import com.idesolution.distribuidoravdc.BD.Dao.VendedorDao;
import com.idesolution.distribuidoravdc.BD.Entity.Cliente;
import com.idesolution.distribuidoravdc.BD.Entity.ClienteSpinnerEntity;
import com.idesolution.distribuidoravdc.BD.Entity.EntregaEntity;
import com.idesolution.distribuidoravdc.BD.Entity.FilaCliente;
import com.idesolution.distribuidoravdc.BD.Entity.LocalizacionEntity;
import com.idesolution.distribuidoravdc.Conexion.MetodosGlobales;
import com.idesolution.distribuidoravdc.Entidad.Constante;
import com.idesolution.distribuidoravdc.Entidad.LoginLocal;
import com.idesolution.distribuidoravdc.IO.ApiAdapter;
import com.idesolution.distribuidoravdc.IO.ApiAdapterSunatReniec;
import com.idesolution.distribuidoravdc.IO.Request.RequestConsultaDoc;
import com.idesolution.distribuidoravdc.IO.Request.RequestSesion;
import com.idesolution.distribuidoravdc.IO.Request.ResquestCliente;
import com.idesolution.distribuidoravdc.IO.Response.ResponseRegistroCliente;
import com.idesolution.distribuidoravdc.IO.Response.ServSunatReniec.ResponseWsReniec;

import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClienteRepository {
    private ClienteDao clienteDao;
    private VendedorDao vendedorDao;
    private LocalizacionDao localizacionDao;
    private LiveData<List<Cliente>> mAllClientes;
    private LiveData<List<Cliente>> mAllClientesSinc;
    private Integer cont;
    private EntregaEntity entrega;
    private String numDoc;
    private EntregaDao entregaDao;
    private int codRuta;
    private Integer ntraUsuario;
    private Integer ntraCliente;
    private Integer ntraLocalizacion;
    private Integer ntraPuntoEntrega;
    ProgressDialog progressDialog;
    private SesionRepository sesionRepository;
    SessionManager sessionManager;
    LoginLocal loginLocal;

    ClienteRepository(Application application) {
        AppDataBase db = AppDataBase.getDatabase(application);
        clienteDao = db.clienteDao();
        entregaDao = db.entregaDao();
        vendedorDao = db.vendedorDao();
		localizacionDao = db.localizacionDao();
        mAllClientes = clienteDao.getClientes();
        mAllClientesSinc = clienteDao.getClientesSin();
        sesionRepository = new SesionRepository(application);
    }

    LiveData<List<Cliente>> getmAllClientes() {
    return mAllClientes;
    }

    LiveData<List<Cliente>> getmAllClientesSinc() {
        return mAllClientesSinc;
    }

    void insert(Cliente cliente,LocalizacionEntity localizacion, Context context) {
        AppDataBase.databaseWriteExecutor.execute(() -> {
            cont = Constante.g_const_0;
            if(cliente.getTipoPersona() == Constante.g_const_2){
                cont = clienteDao.buscarNumeroDocumentoDos(cliente.getRuc());
            }else{
                cont = clienteDao.buscarNumeroDocumentoDos(cliente.getNumeroDocumento());
            }
            if(cont == Constante.g_const_0){
                codRuta = vendedorDao.obtenerCodigoRuta();
                cliente.setCodRuta(Short.parseShort(String.valueOf(codRuta)));
                clienteDao.insert(cliente);
                localizacionDao.insert(localizacion);
                //
                if (cliente.getTipoPersona() == Constante.g_const_2)
                    numDoc = cliente.getRuc();
                else
                    numDoc = cliente.getNumeroDocumento();

                entrega = new EntregaEntity(
                        Constante.g_const_0,
                        null,
                        null,
                        cliente.getDireccion(),
                        null,
                        null,
                        cliente.getTipoPersona(),
                        numDoc,
                        cliente.getCodCliente(),
                        Constante.g_const_0
                );
                entregaDao.insert(entrega);

                registroClientePost(cliente,localizacion, context);
            }


            //
        });
    }

    public void registroClientePost(Cliente cliente,LocalizacionEntity localizacion, Context context){
        //progressDialog = new ProgressDialog(context);
        String ip = "";
        String mac = "";
        sessionManager = new SessionManager(context);
        loginLocal = sessionManager.getUserDetail();
        Integer conexion = MetodosGlobales.verificarConexionInternet(context);
        if(conexion == Constante.g_const_1){
            ntraUsuario = vendedorDao.obtenerNtraUsuario();
            ip = MetodosGlobales.obtenerIP(context);
            mac = MetodosGlobales.obtenerMAC(context);


            ResquestCliente nc = new ResquestCliente(
                    Constante.g_s_const_1,
                    Constante.g_const_0,
                    cliente.getTipoPersona(),
                    cliente.getTipoDocumento(),
                    cliente.getNumeroDocumento(),
                    cliente.getRuc(),
                    cliente.getRazonSocial(),
                    cliente.getNombres(),
                    cliente.getApellidoPaterno(),
                    cliente.getApellidoMaterno(),
                    cliente.getDireccion(),
                    cliente.getCorreo(),
                    cliente.getTelefono(),
                    cliente.getCelular(),
                    cliente.getUbigeo(),
                    null,
                    null,
                    cliente.getClasificacionCliente(),
                    cliente.getFrecuencia(),
                    cliente.getTipoListaPrecio(),
                    cliente.getCodRuta(), //ruta
                    localizacion.getCoordenadaX(),
                    localizacion.getCoordenadaY(),
                    //ntraUsuario.toString(),
                    loginLocal.getNtraUsuario()+ "",
                    ip,
                    mac
            );

            Call<ResponseRegistroCliente> call = ApiAdapter.getApiService().RegistroActualizacionCliente(nc.getProceso(), nc.getCodPersona(), nc.getTipoPersona(), nc.getTipoDocumento(), nc.getNumDocumento(), nc.getRuc(), nc.getRazonSocial(), nc.getNombres(), nc.getApePaterno(), nc.getApeMaterno(), nc.getDireccion(), nc.getCorreo(), nc.getTelefono(), nc.getCelular(), nc.getUbigeo(), nc.getOrdenAtencion(), nc.getPerfilCliente(), nc.getClasificacion(), nc.getFrecuencia(), nc.getTipoListaPrecio(), nc.getCodRuta(),nc.getLatitud(),nc.getLongitud(), nc.getUsuario(), nc.getIp(), nc.getMac());
            call.enqueue(new Callback<ResponseRegistroCliente>() {
                @Override
                public void onResponse(Call<ResponseRegistroCliente> call, Response<ResponseRegistroCliente> response) {
                    if(response.isSuccessful()){
                        ResponseRegistroCliente c = response.body();
                        if(c.getErrorWebSer().getCodigoErr() == Constante.g_const_2000) {
                            AppDataBase.databaseWriteExecutor.execute(() -> {
                                ntraCliente = clienteDao.ntraCliente();
                                ntraLocalizacion = localizacionDao.ntraLocalizacion();
                                clienteDao.actualizarDatosPreventa(c.getResultado().getRegMod().getCodCliente(), ntraCliente);
                                ntraPuntoEntrega = clienteDao.ntraPuntoEntrega();
                                clienteDao.actualizarDatosPuntoEntrega(c.getResultado().getRegMod().getCodPuntoEntrega(), c.getResultado().getRegMod().getCodCliente(), ntraPuntoEntrega);
                                localizacionDao.actualizarDatosLocalizacion(c.getResultado().getRegMod().getCodCliente(), ntraLocalizacion);
                            });
                            Toasty.success(context, "Cliente registrado", Toast.LENGTH_SHORT, true).show();
                            //MetodosGlobales.mostrarMensaje(context, "Cliente registrado");
                            //registrarCliente(c.getResultado().getRegMod().getCodCliente(), Constante.g_const_1);
                        }else if(c.getErrorWebSer().getCodigoErr() == Constante.g_const_1045) {
                            //Toast.makeText(context, data_sincronizacion.getErrorWebSer().getDescripcionErr(), Toast.LENGTH_SHORT).show();
                            Toasty.error(context, c.getErrorWebSer().getDescripcionErr(), Toast.LENGTH_SHORT, true).show();

                            RequestSesion requestSesion = new RequestSesion();
                            requestSesion.setCodUsuario(loginLocal.getNtraUsuario());
                            requestSesion.setTipoLogueo(Constante.g_const_2);
                            requestSesion.setTipoRegistro(Constante.g_const_2);
                            sesionRepository.RegistrarSesionInicio(requestSesion,Constante.g_const_4,context);

                        }
                        else{
                            if(c.getErrorWebSer().getTipoErr() == Constante.g_const_0){
                                MetodosGlobales.mostrarMensaje(context, c.getErrorWebSer().getDescripcionErr());
                                //Toast.makeText(getApplicationContext(), c.getErrorWebSer().getDescripcionErr(), Toast.LENGTH_SHORT).show();
                            }
                            else{
                                MetodosGlobales.mostrarMensaje(context, "Error en el registro del cliente");
                                //Toast.makeText(getApplicationContext(), "Error en el registro del cliente", Toast.LENGTH_SHORT).show();
                            }
                            AppDataBase.databaseWriteExecutor.execute(() -> {
                                ntraCliente = clienteDao.ntraCliente();
                                clienteDao.eliminarCliente(ntraCliente);
                            });
                        }
                    }
                    else{
                        MetodosGlobales.mostrarMensaje(context, "Hubo un error en el registro del cliente");
                        //Toast.makeText(getApplicationContext(), "Hubo un error en el registro del cliente", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseRegistroCliente> call, Throwable t) {
                    MetodosGlobales.mostrarMensaje(context, "Error de conexion");
                    //Toast.makeText(getApplicationContext(), "Error de conexion", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    void update(Integer ntra, String razonSocial, String nombres, String apePaterno, String apeMaterno, String direccion, String correo, String telefono, String celular, Integer flag) {
        AppDataBase.databaseWriteExecutor.execute(() -> {
            clienteDao.update(ntra, razonSocial, nombres, apePaterno, apeMaterno, direccion, correo, telefono, celular, flag);
        });
    }



    LiveData<Integer> buscarNumeroDocumento(String numeroDoc){
        return clienteDao.buscarNumeroDocumento(numeroDoc);
    }

    LiveData<List<FilaCliente>> getListaClientes(){
        return clienteDao.getListaClientes();
    }

    LiveData<List<FilaCliente>> buscarClientes(String cadena){
        return clienteDao.buscarClientes(cadena);
    }

    LiveData<Cliente> buscarCliente(Integer transaccion){
        return clienteDao.buscarCliente(transaccion);
    }

    void deleteAll(){
        AppDataBase.databaseWriteExecutor.execute(() -> {
            clienteDao.deleteAll();
        });
    }

    public void insertList(List<Cliente> lista) {
        AppDataBase.databaseWriteExecutor.execute(() -> {
            clienteDao.deleteAll();
            clienteDao.deleteSequence();
            clienteDao.insertList(lista);
        });
    }

    LiveData<Cliente> buscarClientePreventa(){
        return clienteDao.buscarClientePreventa();
    }

    public LiveData<List<ClienteSpinnerEntity>> obtenerCliente(){return clienteDao.obtenerCliente();}

    public LiveData<Integer> cantidadClientes(){
        return clienteDao.cantidadClientes();
    }

    public void consultaReniec(String numDocumento, Context context) {
        progressDialog = new ProgressDialog(context);
        ConsultarUsuarioReniec(numDocumento,context);
    }

    private void ConsultarUsuarioReniec(String numDoc, Context context) {
        RequestConsultaDoc request;
        request = new RequestConsultaDoc();
        request.setNumDocumento(numDoc);
        Call<ResponseWsReniec> call = ApiAdapterSunatReniec.getApiService().ConsultaDocumentoReniec(request);
        call.enqueue(new Callback<ResponseWsReniec>() {
            @Override
            public void onResponse(Call<ResponseWsReniec> call, Response<ResponseWsReniec> response) {
                if(response.isSuccessful()){
                    ResponseWsReniec rb = response.body();
                    if(rb.getErrorWebSer().getCodigoErr() == Constante.g_const_2000 ) {
                        progressDialog.dismiss();

                    }
                    else{
                        // Toast.makeText(MainActivity.this, rb.getDescripcionResp(), Toast.LENGTH_SHORT).show();
                    }

                }
                else{
                    // Toast.makeText(getApplicationContext(), "Hubo un error en el Servicio", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseWsReniec> call, Throwable t) {
                // Toast.makeText(getApplicationContext(), Constante.g_const_error_conexion, Toast.LENGTH_SHORT).show();

            }
        });
    }

    public LiveData<String> obtenerNombreCliente(int tipo, String cadena){
        if(tipo == Constante.g_const_2)
            return clienteDao.obtenerNombreClienteRuc(cadena);
        else
            return clienteDao.obtenerNombreClienteND(cadena);
    }
}
