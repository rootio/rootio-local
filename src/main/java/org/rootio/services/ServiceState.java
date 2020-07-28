package org.rootio.services;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.utils.Utils;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceState {

    private final int serviceId;
    private int serviceState;
    private Date lastUpdatedDate;
    private String serviceName;

    public ServiceState(int serviceId) {
        this.serviceId = serviceId;
        this.fetchServiceState();
    }

    public ServiceState(int serviceId, String serviceName, int serviceState) {
        this.serviceId = serviceId;
        this.serviceName= serviceName;
        this.serviceState = serviceState;
    }

    /**
     * Gets the state of this service
     *
     * @return Integer representing state of this service. 1: Service is runing,
     * 0: Service is not running
     */
    int getServiceState() {
        // for SMS service. always return true
        if (this.serviceId == 2) {
            return 1;
        }
        return this.serviceState;
    }

    /**
     * Sets the state of the service
     *
     * @param serviceState The integer specifying the state of the service
     */
    public void setServiceState(int serviceState) {
        this.serviceState = serviceState;
        this.save();
    }

    /**
     * Gets the date when the service state was last modified
     *
     * @return Date object representing when service state was last modified
     */
    public Date getLastUpdatedDate() {
        return this.lastUpdatedDate;
    }

    /**
     * Persists the state of the service for consideration across reboots or
     * power failures
     */
    public void save() {
        if(serviceStateExists()) {
            updateServiceState();
        }
        else{
            insertServiceState();
        }
    }

    private void updateServiceState() {
        String tableName = "service_state";
        String updateClause = "state = ? and last_update_date = ?";
        String whereClause = "id = ?";
        List<String> whereArgs = Collections.singletonList(String.valueOf(serviceId));
        List<String> updateArgs = Arrays.asList(String.valueOf(serviceState), Utils.getCurrentDateAsString("yyyy-MM-dd HH:mm:ss"));
        try {
            DBAgent.updateRecords(tableName, updateClause,updateArgs,whereClause,whereArgs);
        } catch (SQLException e) {
            Logger.getLogger("RootIO").log(Level.INFO, e.getMessage() == null ? "Null pointer[ServiceState.updateServiceState]" : e.getMessage());
        }
    }

    /**
     * Fetches the state of the service as persisted in the database
     */
    private void fetchServiceState() {
        String query = "select service, state, last_update_date from service_state where id = ?";
        List<String> whereArgs = Collections.singletonList(String.valueOf(serviceId));
        List<List<Object>> result = null;
        try {
            result = DBAgent.getData(query, whereArgs);
        } catch (SQLException e) {
            Logger.getLogger("RootIO").log(Level.INFO, e.getMessage() == null ? "Null pointer[ServiceState.fetchServiceState]" : e.getMessage());
        }
        this.serviceState = result !=null && result.size() > 0 ? (int)result.get(0).get(1) : 0;
    }

    private void insertServiceState()
    {
        String tableName = "service_state";
        HashMap<String, Object> data = new HashMap<>();
        data.put("id", serviceId);
        data.put("service", serviceName);
        data.put("service_state", serviceState);
        data.put("last_updated_date", Utils.getCurrentDateAsString("yyyy-MM-dd HH:mm:ss"));
        try {
            DBAgent.saveData(tableName, data);
        } catch (SQLException e) {
            Logger.getLogger("RootIO").log(Level.INFO, e.getMessage() == null ? "Null pointer[ServiceState.insertServiceState]" : e.getMessage());
        }
    }

    private boolean serviceStateExists()
    {
        String query = "select * from service_state where id = ?";
        List<String> whereArgs = Collections.singletonList(String.valueOf(serviceId));
        List<List<Object>> result = null;
        try {
            result = DBAgent.getData(query, whereArgs);
        } catch (SQLException e) {
            Logger.getLogger("RootIO").log(Level.INFO, e.getMessage() == null ? "Null pointer[ServiceState.serviceStateExists]" : e.getMessage());
        }
        return result != null && result.size() > 0;
    }
}
