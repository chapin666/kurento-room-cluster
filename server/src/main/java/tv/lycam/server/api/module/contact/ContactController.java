package tv.lycam.server.api.module.contact;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tv.lycam.server.api.module.comm.BaseController;
import tv.lycam.server.api.response.ResponseModel;

import java.util.List;

/**
 * Created by lycamandroid on 2017/6/19.
 */
@RestController
@RequestMapping("/contact")
public class ContactController extends BaseController {


    @Autowired
    private ContactRepository contactRepository;


    /**
     *
     * @param model
     * @return
     */
    @PostMapping
    public ResponseEntity add(@RequestBody ContactModel model) {
        ContactModel contactModel = contactRepository.insert(model);
        return ResponseEntity.ok(new ResponseModel<>(contactModel));
    }


    /**
     *
     * @return
     */
    @GetMapping
    public ResponseEntity list() {
        List<ContactModel> contacts = contactRepository.findAll();
        return ResponseEntity.ok(new ResponseModel<>(contacts));
    }


    /**
     *
     * @return
     */
    @GetMapping("/{contactId}")
    public ResponseEntity findById(@PathVariable("contactId") String contactId) {
        ContactModel contactModel = contactRepository.findOne(contactId);

        if (contactModel != null) {
            return ResponseEntity.ok(new ResponseModel<>(contactModel));
        } else {
            return ResponseEntity.ok(new ResponseModel<>("没有查询到该联系人"));
        }
    }


    /**
     *
     * @param model
     * @return
     */
    @PutMapping
    public ResponseEntity update(ContactModel model) {
        ContactModel contactModel = contactRepository.save(model);
        if (contactModel != null) {
            return ResponseEntity.ok(new ResponseModel<>(contactModel));
        } else {
            return ResponseEntity.ok(new ResponseModel<>("没有查询到该联系人"));
        }
    }




}
