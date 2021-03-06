package com.idesolution.distribuidoravdc.Fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.idesolution.distribuidoravdc.Adaptador.MenuListAdapter;
import com.idesolution.distribuidoravdc.BD.Repository.BitacoraVendedorViewModel;
import com.idesolution.distribuidoravdc.BD.Repository.ClienteViewModel;
import com.idesolution.distribuidoravdc.BD.Repository.VendedorViewModel;
import com.idesolution.distribuidoravdc.Conexion.MetodosGlobales;

import com.idesolution.distribuidoravdc.R;
import com.idesolution.distribuidoravdc.UI.mapa_ubicacion_clientes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class f_lista_menu extends Fragment {

    private RecyclerView recyclerMenu;
    private MenuListAdapter adapter;
    private FragmentTransaction ft;
    ArrayList<String> list = new ArrayList<>();
    private ClienteViewModel clienteVM;
    Integer cantidadc;
    private Integer ivisita;
    private Integer codr;
    private Integer estadoRA;
    private Integer ntraUsuario = 1;
    private VendedorViewModel RutaVM;
    BitacoraVendedorViewModel bitacoraVendedorViewModel;

    public f_lista_menu() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_f_lista_menu, container, false);

        getActivity().setTitle("Menu");
        recyclerMenu = view.findViewById(R.id.rvListaMenu);
        adapter = new MenuListAdapter(getContext());
        recyclerMenu.setAdapter(adapter);
        recyclerMenu.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //list = new ArrayList<>();

        list.add("RUTAS ASIGNADAS");
        list.add("REPORTES");
        list.add("MAPA DE CLIENTES");
        adapter.setOpcionesMenu(list);
        LimpiarBitacoras();
        obtenerCantidadCliente();
    }

    public void onclick(int cantidad){

        adapter.setOnClickListener(v ->     {

            if(recyclerMenu.getChildAdapterPosition(v) == 0) {

               // MetodosGlobales.mostrarMensaje(getContext(), "click" + cantidad);

                if(cantidad > 0){
                ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.frame_principal, new f_listar_ruta_asignada());
               ft.addToBackStack(null);
                ft.commit();
                list.clear();

                }else{
                        MetodosGlobales.mostrarMensaje(getContext(), "No hay clientes registrados en esta ruta");
                    }
                }

            if(recyclerMenu.getChildAdapterPosition(v) == 2) {

                Intent intent = new Intent(getContext(), mapa_ubicacion_clientes.class);
                startActivity(intent);

            }
        });

    }



    public void obtenerCantidadCliente(){

        clienteVM = new ViewModelProvider(this).get(ClienteViewModel.class);
        clienteVM.cantidadClientes().observe(f_lista_menu.this, cantidc ->{
            if(cantidc != null){
                cantidadc = cantidc;
            }
            onclick(cantidadc);
        });

    }

    public void LimpiarBitacoras(){
        bitacoraVendedorViewModel = new ViewModelProvider(this).get(BitacoraVendedorViewModel.class);
        bitacoraVendedorViewModel.deleteAll();
    }

}
