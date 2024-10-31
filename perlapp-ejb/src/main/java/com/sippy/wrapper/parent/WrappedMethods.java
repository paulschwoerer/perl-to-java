package com.sippy.wrapper.parent;

import com.sippy.wrapper.parent.database.DatabaseConnection;
import com.sippy.wrapper.parent.database.dao.TnbDao;
import com.sippy.wrapper.parent.request.GetTnbListRequest;
import com.sippy.wrapper.parent.request.JavaTestRequest;
import com.sippy.wrapper.parent.response.GetTnbListResponse;
import com.sippy.wrapper.parent.response.JavaTestResponse;

import java.util.*;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sippy.wrapper.parent.response.GetTnbListResponse.*;

@Stateless
public class WrappedMethods {

  private static final Logger LOGGER = LoggerFactory.getLogger(WrappedMethods.class);

  @EJB DatabaseConnection databaseConnection;

  @PersistenceContext(unitName = "CustomDB")
  private EntityManager entityManager;

  @RpcMethod(name = "javaTest", description = "Check if everything works :)")
  public Map<String, Object> javaTest(JavaTestRequest request) {
    JavaTestResponse response = new JavaTestResponse();

    int count = databaseConnection.getAllTnbs().size();

    LOGGER.info("the count is: " + count);

    response.setId(request.getId());
    String tempFeeling = request.isTemperatureOver20Degree() ? "warm" : "cold";
    response.setOutput(
        String.format(
            "%s has a rather %s day. And he has %d tnbs", request.getName(), tempFeeling, count));

    Map<String, Object> jsonResponse = new HashMap<>();
    jsonResponse.put("faultCode", "200");
    jsonResponse.put("faultString", "Method success");
    jsonResponse.put("something", response);

    return jsonResponse;
  }

  @RpcMethod(name = "getTnbList")
  public GetTnbListResponse getTnbList(GetTnbListRequest request) {
    LOGGER.info("Fetching TNB list from the database");

    @SuppressWarnings("unchecked")
    final List<TnbDao> tnbsFromDB = entityManager.createNativeQuery("SELECT * FROM tnbs", TnbDao.class).getResultList();

    final String number = request.number();

    String tnb = null;
    if (number != null) {
      try {
        tnb = (String) entityManager.createNativeQuery("SELECT tnb FROM tnbs WHERE tnb = :tnb")
                .setParameter("tnb", number)
                .getSingleResult();
      } catch (NoResultException e) {
        // ignored
      }
    }

    final List<Tnb> tnbs = new ArrayList<>();
    tnbs.add(new Tnb("D001", "Deutsche Telekom", "D001".equals(tnb)));

    for (TnbDao tnbDao : tnbsFromDB) {
      if (tnbDao.getTnb().matches("(D146|D218|D248)")) {
        continue;
      }

      tnbs.add(new Tnb(tnbDao.getTnb(), tnbDao.getName(), tnbDao.getTnb().equals(tnb)));
    }

    tnbs.sort(Comparator.comparing(a -> a.name().toLowerCase()));

    return new GetTnbListResponse("200", "Method success", tnbs);
  }
}
